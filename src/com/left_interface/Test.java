package com.left_interface;

import com.system.model.Disk;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.Scanner;

import static javafx.application.Application.launch;

public class Test extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        AnchorPane root = new AnchorPane();
        Disk disk = Disk.getInstance();
        VirtualDiskTable virtualDiskTable = new VirtualDiskTable();
       // disk.addObserver(virtualDiskTable);
        byte[] bytes = {1,2};
       // disk.writeBlock(1,bytes);
        root.getChildren().add(virtualDiskTable.getVirtualDiskTable());
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
