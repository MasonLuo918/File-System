package com.left_interface;

import com.system.entity.OpenFileTable;
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
        Scene scene = new Scene(root);
        Disk disk = Disk.getInstance();
        VirtualDiskTable virtualDiskTable = new VirtualDiskTable(scene);
        OpenFileTableView openFileTableView = new OpenFileTableView(scene);
       // disk.addObserver(virtualDiskTable);
        byte[] bytes = {1,2};
       // disk.writeBlock(1,bytes);
        root.getChildren().addAll(virtualDiskTable.getVirtualDiskTable(), openFileTableView.getOpenFileTableVew());
        AnchorPane.setTopAnchor(openFileTableView.getOpenFileTableVew(),500.0);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(800);
        primaryStage.show();

    }
}
