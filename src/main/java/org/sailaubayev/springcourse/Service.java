package org.sailaubayev.springcourse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

public class Service {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode rootNode = objectMapper.createObjectNode();
    Scanner sc = new Scanner(System.in);

    public Service() {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void jsonRead() {
        try{
            File file = new File("Tasks.json");
            if (!file.exists()) {
                System.out.println("No tasks yet");
                return;
            }
            ObjectNode node = (ObjectNode) objectMapper.readTree(file);
            System.out.println(
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
            );
        }catch (IOException e){
            System.out.println("File not founded");
        }
        run();
    }

    public void jsonAdd() {
        try {

            File file = new File("Tasks.json");

            if (file.exists()) {
                rootNode = (ObjectNode) objectMapper.readTree(file);
            }

            //auto id
            int nextId = 1;
            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                try {
                    int currentId = Integer.parseInt(field);
                    nextId = Math.max(nextId, currentId + 1);
                }catch(NumberFormatException e){

                }
            }

            // user input
            System.out.print("Write Description: ");
            String description = sc.nextLine();

            System.out.print("Write status (TODO, IN_PROGRESS, DONE): ");
            String input = sc.nextLine().trim().toUpperCase();
            Status status;
            try {
                status = Status.valueOf(input.replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status value");
                return;
            }



            // create task
            Task task = new Task(
                    nextId,
                    description,
                    status,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            ObjectNode taskNode = objectMapper.valueToTree(task);
            rootNode.set(String.valueOf(task.getId()), taskNode);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

            System.out.println("Task added with id = " + nextId);
        }catch (IOException e){
            System.out.println("File not founded");
        }
        run();
    }

    public void jsonUpdate(){
        try {
            File file = new File("Tasks.json");


            if (file.exists()) {
                rootNode = (ObjectNode) objectMapper.readTree(file);
            }


            System.out.print("Choose id of task to update: ");
            int userInputId = Integer.parseInt(sc.nextLine());
            String idAsString = String.valueOf(userInputId);

            if (!rootNode.has(idAsString)) {
                System.out.println("Task with ID " + userInputId + " not found");
                return;
            }

            ObjectNode taskNode = (ObjectNode) rootNode.get(idAsString);
            Task existingTask = objectMapper.treeToValue(taskNode, Task.class);

            System.out.println("Current task: " + existingTask.getDescription());
            System.out.println("Current status " + existingTask.getStatus());

            System.out.println("\nEnter new data (press Enter to keep current value):");

            System.out.println("Write Description [" + existingTask.getDescription() + "]: ");

            String description = sc.nextLine();
            if (description.trim().isEmpty()) {
                description = existingTask.getDescription();
            }

            System.out.print("Write status (TODO, IN_PROGRESS, DONE) [" + existingTask.getStatus() + "]: ");
            String input = sc.nextLine().trim().toUpperCase();
            Status status;

            if (input.isEmpty()) {
                status = existingTask.getStatus();
            } else {
                try {
                    status = Status.valueOf(input.replace(" ", "_"));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid status value. Keeping current status");
                    status = existingTask.getStatus();
                }
            }
            Task updatedTask = new Task(
                    userInputId,
                    description,
                    status,
                    existingTask.getCreatedAt(),
                    LocalDateTime.now()
            );

            ObjectNode updatedTaskNode = objectMapper.valueToTree(updatedTask);
            rootNode.set(idAsString, updatedTaskNode);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

            System.out.println("Task updated with id = " + userInputId);
        }catch(NumberFormatException e) {
            System.out.println("Please enter a valid number for ID");
        }catch (IOException e) {
            System.out.println("Error reading/writing file: " + e.getMessage());
        }catch (Exception e){
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }finally {
            run();
        }


    }

    public void jsonDelete(){
        try {
            File file = new File("Tasks.json");


            if (file.exists()) {
                rootNode = (ObjectNode) objectMapper.readTree(file);
            }

            if (rootNode.isEmpty()){
                System.out.println("No tasks to delete. File is empty");
                return;
            }

            System.out.print("Choose id of task to delete: ");
            int userInputId = Integer.parseInt(sc.nextLine());
            String idAsString = String.valueOf(userInputId);

            if (!rootNode.has(idAsString)) {
                System.out.println("Task with ID " + userInputId + " not found");
                return;
            }


            ObjectNode taskToDelete = (ObjectNode) rootNode.get(idAsString);
            System.out.println("Task to delete:");
            System.out.println("Description: " + taskToDelete.get("description"));
            System.out.println("Status: " + taskToDelete.get("status"));

            System.out.print("\nAre you sure you want to delete task " + userInputId + "? (y/n): ");
            String confirmation = sc.nextLine().trim().toLowerCase();
            if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                System.out.println("Deletion cancelled");
                return;
            }

            rootNode.remove(idAsString);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

            System.out.println("Task with ID " + userInputId + " deleted successfully");

        }catch(NumberFormatException e) {
            System.out.println("Please enter a valid number for ID");
        }catch (IOException e) {
            System.out.println("Error reading/writing file: " + e.getMessage());
        }catch (Exception e){
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }finally {
            run();
        }
    }
    public void run(){
        System.out.print("""
                ===Task Tracker===
                Choose operation:
                1) Read all Tasks
                2) Add a new Task
                3) Update a task
                4) Delete a task
                5) Stop program
                
                Write number: """);
        String input = sc.nextLine();
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number");
            return;
        }
        switch(choice){
            case 1:
                jsonRead();
                break;
            case 2:
                jsonAdd();
                break;
            case 3:
                jsonUpdate();
                break;
            case 4:
                jsonDelete();
                break;
            case 5:
                break;
            default:
                System.out.println("write number");
        }
    }

}
