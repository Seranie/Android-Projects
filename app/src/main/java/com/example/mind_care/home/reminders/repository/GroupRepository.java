package com.example.mind_care.home.reminders.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.mind_care.notification.ScheduleNotification;
import com.example.mind_care.home.reminders.model.ReminderItemModel;
import com.example.mind_care.home.reminders.model.RemindersGroupItem;
import com.example.mind_care.home.reminders.viewModel.ReminderGroupViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GroupRepository {
    private final FirebaseFirestore db;
    private final String collection = "users";
    private final FirebaseUser user;
    List<RemindersGroupItem> groupList = new ArrayList<>();

    public GroupRepository() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void addNewGroup(RemindersGroupItem groupItem) {
        db.collection(collection).document(user.getUid()).collection("groups").add(groupItem).addOnCompleteListener(document -> {
            if (document.isSuccessful()) {
                Log.i("INFO", "Group added");
                String groupId = document.getResult().getId();
                document.getResult().update("groupId", groupId);
            } else {
                Log.i("INFO", String.valueOf(document.getException()));
            }
        });
    }

    public CompletableFuture<Boolean> deleteGroup(Context activityContext, RemindersGroupItem groupItem) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        db.collection(collection)
                .document(user.getUid())
                .collection("groups")
                .document(groupItem.getGroupId())
                .get()
                .addOnCompleteListener(groupTask -> {
                    if (groupTask.isSuccessful() && groupTask.getResult() != null) {
                        DocumentReference groupDoc = groupTask.getResult().getReference();

                        // Fetch all reminders before deletion
                        groupDoc.collection("reminders")
                                .get()
                                .addOnCompleteListener(reminderTask -> {
                                    if (reminderTask.isSuccessful()) {
                                        // Track deletions
                                        List<CompletableFuture<Void>> deletionFutures = new ArrayList<>();

                                        // Delete each reminder in the subcollection
                                        for (DocumentSnapshot reminderDoc : reminderTask.getResult()) {
                                            //Unschedule alarms for each reminder.
                                            reminderDoc.getReference().collection("alertItems").get().addOnCompleteListener(alertItemTask -> {
                                               if(alertItemTask.isSuccessful()) {
                                                   List<String> alertItemIds = new ArrayList<>();
                                                   for (DocumentSnapshot alertItemDoc : alertItemTask.getResult()) {
                                                       alertItemIds.add(alertItemDoc.getId());
                                                       alertItemDoc.getReference().delete();
                                                   }
                                                   ScheduleNotification.cancelNotification(activityContext, reminderDoc.getId(), alertItemIds);
                                               }
                                            });



                                            CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
                                            reminderDoc.getReference().delete().addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    deletionFuture.complete(null);
                                                } else {
                                                    deletionFuture.completeExceptionally(deleteTask.getException());
                                                }
                                            });
                                            deletionFutures.add(deletionFuture);
                                        }

                                        // After all reminders are deleted, delete the group document
                                        CompletableFuture.allOf(deletionFutures.toArray(new CompletableFuture[0]))
                                                .thenRun(() -> {
                                                    groupDoc.delete().addOnCompleteListener(groupDeleteTask -> {
                                                        if (groupDeleteTask.isSuccessful()) {
                                                            completableFuture.complete(true);
                                                        } else {
                                                            completableFuture.complete(false);
                                                        }
                                                    });
                                                }).exceptionally(ex -> {
                                                    // If any deletion fails, complete with false
                                                    completableFuture.complete(false);
                                                    return null;
                                                });

                                    } else {
                                        completableFuture.complete(false); // Could not fetch reminders
                                    }
                                });

                    } else {
                        completableFuture.complete(false); // Could not fetch group
                    }
                });

        return completableFuture;
    }

    public void retrieveGroupList(OnCompleteCallback callback) {
        db.collection(collection).document(user.getUid()).collection("groups").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();

                if (!docs.isEmpty()) {
                    List<RemindersGroupItem> tempList = new ArrayList<>();
                    List<Task<?>> allTasks = new ArrayList<>(); // Collect all Firestore tasks

                    for (DocumentSnapshot shot : docs) {
                        Uri uri = Uri.parse(String.valueOf(shot.get("imageSource")));
                        String name = String.valueOf(shot.get("name"));
                        RemindersGroupItem groupItem = new RemindersGroupItem(uri, name);
                        groupItem.setGroupId(shot.getId());

                        // Query for reminders under the group
                        CollectionReference colRef = shot.getReference().collection("reminders");
                        Task<QuerySnapshot> reminderTask = colRef.get();
                        allTasks.add(reminderTask);

                        reminderTask.addOnCompleteListener(reminderTaskResult -> {
                            if (reminderTaskResult.isSuccessful()) {
                                List<ReminderItemModel> reminders = new ArrayList<>();
                                for (DocumentSnapshot reminderShot : reminderTaskResult.getResult()) {
                                    String groupId = shot.getId();
                                    String title = (String) reminderShot.get("title");
                                    String note = (String) reminderShot.get("note");
                                    Timestamp timestamp = reminderShot.getTimestamp("schedule");
                                    LocalDateTime dateTime = LocalDateTime.now();
                                    if (timestamp != null) {
                                        Date date = timestamp.toDate();
                                        dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                                    }

                                    // Query for alert items under each reminder
                                    CollectionReference alertRef = reminderShot.getReference().collection("alertItems");
                                    Task<QuerySnapshot> alertTask = alertRef.get();
                                    allTasks.add(alertTask);

                                    LocalDateTime finalDateTime = dateTime;
                                    alertTask.addOnCompleteListener(alertTaskResult -> {
                                        if (alertTaskResult.isSuccessful()) {
                                            List<LocalDateTime> alertItemList = new ArrayList<>();
                                            for (DocumentSnapshot alertShot : alertTaskResult.getResult()) {
                                                Date alertDate = new Date();
                                                Timestamp alertTimestamp = alertShot.getTimestamp("dateTime");
                                                if (alertTimestamp != null) {
                                                    alertDate = alertTimestamp.toDate();
                                                }
                                                LocalDateTime alertDateTime = LocalDateTime.ofInstant(alertDate.toInstant(), ZoneId.systemDefault());
                                                alertItemList.add(alertDateTime);
                                            }

                                            // Create reminder item after alert items have been fetched
                                            ReminderItemModel reminderItem = new ReminderItemModel(groupId, title, note, finalDateTime, alertItemList);
                                            reminders.add(reminderItem);
                                        }
                                    });
                                }

                                groupItem.setReminderList(reminders);
                                tempList.add(groupItem);
                            }
                        });
                    }

                    // Wait for all asynchronous Firestore tasks to complete
                    Tasks.whenAllComplete(allTasks).addOnCompleteListener(finalTask -> {
                        groupList = tempList;  // Update groupList only after all tasks are completed
                        callback.onComplete(groupList);  // Call the callback
                    });

                } else {
                    callback.onComplete(new ArrayList<>());  // Empty list if no documents
                }
            } else {
                Log.i("INFO", String.valueOf(task.getException()));
            }
        });
    }


//    public CompletableFuture<Boolean> deleteGroup(RemindersGroupItem groupItem){
//        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
//        db.collection(collection).document(user.getUid()).collection("groups").document(groupItem.getGroupId()).get().addOnCompleteListener(groupTask ->{
//            if(groupTask.isSuccessful()){
//                DocumentReference groupDoc = groupTask.getResult().getReference();
//                groupDoc.collection("reminders").get().addOnCompleteListener(reminderTask -> {
//                    if(reminderTask.isSuccessful()){
//                        for(DocumentSnapshot reminderDoc : reminderTask.getResult()){
//                            reminderDoc.getReference().delete();
//                        }
//                    }
//                });
//                groupDoc.delete().addOnCompleteListener(deleteGroupTask -> {
//                    Log.i("INFO", "HUH");
//                    completableFuture.complete(true);
//                });
//            }else{
//                completableFuture.completeExceptionally(groupTask.getException());
//            }
//        });
//        return completableFuture;
//    }

//    public void deleteGroup(RemindersGroupItem groupItem) {
//        db.collection(collection).document(user.getUid()).update("groups", FieldValue.arrayRemove(groupItem));
//    }

//    public void retrieveGroupList(OnCompleteCallback callback) {
//        db.collection(collection).document(user.getUid()).collection("groups").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<DocumentSnapshot> docs = task.getResult().getDocuments();
//
//                if (!docs.isEmpty()) {
//                    List<RemindersGroupItem> tempList = new ArrayList<>();
//                    for (DocumentSnapshot shot : docs) {
//                        //Query all group under groups, and create new GroupItem. groups -> group
//                        //TODO change groupitem to have a list of reminders under them.
//                        Uri uri = Uri.parse(String.valueOf(shot.get("imageSource")));
//                        String name = String.valueOf(shot.get("name"));
//                        RemindersGroupItem groupItem = new RemindersGroupItem(uri, name);
//                        groupItem.setGroupId(shot.getId());
//
//                        //Further query group for reminders. group -> reminders
//                        CollectionReference colRef = shot.getReference().collection("reminders");
//                        List<ReminderItemModel> reminders = new ArrayList<>();
//                        colRef.get().addOnCompleteListener(reminderTask -> {
//                            if (reminderTask.isSuccessful()){
//                                for (DocumentSnapshot reminderShot : reminderTask.getResult()) {
//                                    String groupId = shot.getId();
//                                    String title = (String) reminderShot.get("title");
//                                    String note = (String) reminderShot.get("note");
//                                    Timestamp timestamp = reminderShot.getTimestamp("schedule");
//                                    LocalDateTime dateTime = LocalDateTime.now();
//                                    if(timestamp != null){
//                                        Date date = timestamp.toDate();
//                                        dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//                                    }
//
//
//                                    //For each reminder, query for alert items reminder -> datetime
//                                    CollectionReference alertRef = reminderShot.getReference().collection("alertItems");
//                                    List<LocalDateTime> alertItemList = new ArrayList<>();
//                                    alertRef.get().addOnCompleteListener(alertTask -> {
//                                        if (alertTask.isSuccessful()){
//                                            for (DocumentSnapshot alertShot : alertTask.getResult()) {
//                                                Date alertDate = new Date();
//                                                Timestamp alertTimestamp = alertShot.getTimestamp("dateTime");
//                                                if (alertTimestamp != null){
//                                                 alertDate= alertTimestamp.toDate();
//
//                                                }
//                                                LocalDateTime alertDateTime = LocalDateTime.ofInstant(alertDate.toInstant(), ZoneId.systemDefault());
//                                                alertItemList.add(alertDateTime);
//                                            }
//                                        }
//                                    });
//
//                                    ReminderItemModel reminderItem = new ReminderItemModel(groupId, title, note, dateTime, alertItemList);
//                                    reminders.add(reminderItem);
//                                }
//                            }
//                        });
//
//                        groupItem.setReminderList(reminders);
//                        tempList.add(groupItem);
//                    }
//                    groupList = tempList;
//                }
//                callback.onComplete(groupList);
//            } else {
//                Log.i("INFO", String.valueOf(task.getException()));
//            }
//        });
//    }

    public void retrieveRemindersFromGroup(String groupId, OnReminderCompleteCallback callback) {
        List<ReminderItemModel> tempList = new ArrayList<>();

        db.collection(collection).document(user.getUid()).collection("groups").document(groupId).collection("reminders").get().addOnCompleteListener(task ->{
            if(task.isSuccessful()){
                for (DocumentSnapshot doc : task.getResult().getDocuments()){
                    //Get all alert items and make a list of it
                    List<LocalDateTime> alertItemList = new ArrayList<>();
                    doc.getReference().collection("alertItems").get().addOnCompleteListener(getAlertItemsTask -> {
                        for (DocumentSnapshot alertItemDoc : getAlertItemsTask.getResult().getDocuments()){
                            alertItemList.add(alertItemDoc.getTimestamp("dateTime").toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        }
                    });
                    //Make new reminder item
                    Map<String, Object> map = doc.getData();
                    Timestamp timestamp = (Timestamp) map.get("schedule");
                    LocalDateTime dateTime = LocalDateTime.now();
                    Date date = timestamp.toDate();
                    dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

                    ReminderItemModel reminderItem = new ReminderItemModel(groupId, (String) map.get("title"), (String) map.get("note"), dateTime, alertItemList);
                    Timestamp createdDate = (Timestamp) map.get("createdDate");
                    reminderItem.setCreatedDate(createdDate);
                    reminderItem.setReminderId(doc.getId());
                    tempList.add(reminderItem);
                }
                callback.onComplete(tempList);
            } else {
                Log.i("INFO", String.valueOf(task.getException()));
            }
        });
    }

    public void deleteReminder(String groupId, String reminderId){
        DocumentReference reminderDoc = db.collection(collection).document(user.getUid()).collection("groups").document(groupId).collection("reminders").document(reminderId);
        reminderDoc.collection("alertItems").get().addOnCompleteListener(alertItemTask -> {
            if(alertItemTask.isSuccessful()){
                for (DocumentSnapshot alertItemDoc : alertItemTask.getResult().getDocuments()){
                    alertItemDoc.getReference().delete();
                }
            }
        });
        reminderDoc.delete();
    }

    public void getAllRemindersAndSetNotifications(Context context){
        db.collection(collection).document(user.getUid()).collection("groups").get().addOnCompleteListener(task ->{
           if(task.isSuccessful()){
               for (DocumentSnapshot groupDoc : task.getResult()){
                   groupDoc.getReference().collection("reminders").get().addOnCompleteListener(reminderTask -> {
                       if(reminderTask.isSuccessful()){
                           for (DocumentSnapshot reminderDoc : reminderTask.getResult()){
                               ScheduleNotification.scheduleNotification(context, reminderDoc.getTimestamp("schedule"), reminderDoc.getString("title"), reminderDoc.getString("note"), reminderDoc.getId());
                               reminderDoc.getReference().collection("alertItems").get().addOnCompleteListener(alertItemTask -> {
                                   if(alertItemTask.isSuccessful()){
                                       for (DocumentSnapshot alertItemDoc : alertItemTask.getResult()){
                                           ScheduleNotification.scheduleNotification(context, alertItemDoc.getTimestamp("dateTime"), reminderDoc.getString("title"), reminderDoc.getString("note"), reminderDoc.getId(), alertItemDoc.getId());
                                       }
                                   }
                               });
                           }
                       }
                   });


               }
           }
        });
    }

    public void getAllAlertItemIds(String groupId, String reminderId, ReminderGroupViewModel.OnAlertItemIdQueryComplete callback){
        List<String> alertItemIds = new ArrayList<>();
        db.collection(collection).document(user.getUid()).collection("groups").document(groupId).collection("reminders").document(reminderId).collection("alertItems").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for (DocumentSnapshot doc : task.getResult().getDocuments()){
                    alertItemIds.add(doc.getId());
                }
                Log.i("INFO","AlertItemId's "  + alertItemIds);
                callback.onComplete(alertItemIds);
            }
        });
    }


    public interface OnCompleteCallback {
        void onComplete(List<RemindersGroupItem> groupList);
    }

    public interface OnReminderCompleteCallback {
        void onComplete(List<ReminderItemModel> reminderList);
    }


}
