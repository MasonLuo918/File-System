package com.left_interface;

import com.system.entity.OpenFile;
import com.system.entity.OpenFileTable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.Observable;
import java.util.Observer;

public class OpenFileTableView implements Observer {

    private TableView openFileTableVew = null;
    private ObservableList<OpenFileTable> list = null;
    private OpenFile openFile = null;
    private Scene scene = null;

    public OpenFileTableView(Scene scene){
        this.scene = scene;
        openFile = OpenFile.getInstance();
        list = FXCollections.observableArrayList(OpenFile.getData());
        openFileTableVew = new TableView(list);
        openFileTableVew.prefHeightProperty().bind(scene.heightProperty().multiply(0.6));
        openFileTableVew.prefWidthProperty().bind(scene.widthProperty().multiply(0.25));
        init();
    }

    public void init(){
        TableColumn<OpenFileTable, String> name = new TableColumn<OpenFileTable, String>("�ļ�·����");
        TableColumn<OpenFileTable, Number> attribute = new TableColumn<OpenFileTable, Number>("�ļ�����");
        TableColumn<OpenFileTable, Number> number = new TableColumn<OpenFileTable, Number>("��ʼ�̿��");
        TableColumn<OpenFileTable, Number> length = new TableColumn<OpenFileTable, Number>("�ļ�����");
        TableColumn<OpenFileTable, Number> flag = new TableColumn<OpenFileTable, Number>("��������");
        TableColumn<OpenFileTable, Number> r_dnum = new TableColumn<OpenFileTable, Number>("��ţ�����");
        TableColumn<OpenFileTable, Number> r_bnum = new TableColumn<OpenFileTable, Number>("���ڵ�ַ������");
        TableColumn<OpenFileTable, Number> w_dnum = new TableColumn<OpenFileTable, Number>("��ţ�д��");
        TableColumn<OpenFileTable, Number> w_bnum = new TableColumn<OpenFileTable, Number>("���ڵ�ַ��д��");
        openFileTableVew.getColumns().addAll(name, attribute, number, length, flag, r_bnum, r_dnum, w_bnum, w_dnum);

        openFileTableVew.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //��ÿһ���������
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
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getStartBlockNum());
                return s;
            }
        });
        length.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getStartBlockNum());
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
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getRead().getBnum());
                return s;
            }
        });
        r_dnum.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getRead().getDnum());
                return s;
            }
        });

        w_bnum.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getWrite().getBnum());
                return s;
            }
        });
        w_dnum.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<OpenFileTable, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<OpenFileTable, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getWrite().getDnum());
                return s;
            }
        });

    }
    @Override
    public void update(Observable o, Object openFile){
        openFileTableVew.refresh();
    }

    public TableView getOpenFileTableVew() {
        return openFileTableVew;
    }

    public void setOpenFileTableVew(TableView openFileTableVew) {
        this.openFileTableVew = openFileTableVew;
    }

    public ObservableList<OpenFileTable> getList() {
        return list;
    }

    public void setList(ObservableList<OpenFileTable> list) {
        this.list = list;
    }

    public OpenFile getOpenFile() {
        return openFile;
    }

    public void setOpenFile(OpenFile openFile) {
        this.openFile = openFile;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
