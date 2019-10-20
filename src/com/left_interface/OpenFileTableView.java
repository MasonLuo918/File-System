package com.left_interface;

import com.system.entity.OpenFileTable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class OpenFileTableView implements Observer {
    private TableView openFileTableVew = null;
    private ObservableList<OpenFileTable> list = null;

    public OpenFileTableView(ArrayList<OpenFileTable> openFile){
        list = FXCollections.observableArrayList(openFile.getData);
        openFileTableVew = new TableView(list);
        init();
    }

    public void init(){
        TableColumn<OpenFileTable, String> name = new TableColumn<OpenFileTable, String>("文件路径名");
        TableColumn<OpenFileTable, Number> attribute = new TableColumn<OpenFileTable, Number>("文件属性");
        TableColumn<OpenFileTable, Number> number = new TableColumn<OpenFileTable, Number>("起始盘块号");
       // TableColumn<OpenFileTable, Number> length = new TableColumn<OpenFileTable, Number>("文件长度");
        TableColumn<OpenFileTable, Number> flag = new TableColumn<OpenFileTable, Number>("操作类型");
        TableColumn<OpenFileTable, Number> r_dnum = new TableColumn<OpenFileTable, Number>("块号（读）");
        TableColumn<OpenFileTable, Number> r_bnum = new TableColumn<OpenFileTable, Number>("块内地址（读）");
        TableColumn<OpenFileTable, Number> w_dnum = new TableColumn<OpenFileTable, Number>("块号（写）");
        TableColumn<OpenFileTable, Number> w_bnum = new TableColumn<OpenFileTable, Number>("块内地址（写）");

        name.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<OpenFileTable, String> param) {
                SimpleStringProperty s = new SimpleStringProperty(param.getValue().getName());
                return s;
            }
        });


        attribute.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getAttribute());
                return s;
            }
        });

        number.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getNumber());
                return s;
            }
        });

        flag.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getFlag());
                return s;
            }
        });

        r_bnum.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getRead())
            }
        });

    }
    @Override
    public void update(Observable o, Object openFile){

    }

}
