package com.left_interface;

import com.system.model.Disk;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.Observable;
import java.util.Observer;

/**
 * @author QTJ
 */
public class VirtualDiskTable implements Observer {
    //磁盘表
    private TableView virtualDiskTable ;
    //存放磁盘使用情况的表格
    private ObservableList<VirtualDisk> list;
    private Disk disk = Disk.getInstance();


    public VirtualDiskTable(){
        //创建一个ObservableList实例
        this.list = FXCollections.observableArrayList();
        //以list作为数据源创建一个TableView实例
        this.virtualDiskTable = new TableView(this.list);
        init();
    }

    /**
     * 但被观察者改动时，该方法会被调用
     * @param o  被观察对象
     * @param space 被观察对象传回来的磁盘
     */
    @Override
    public void update(Observable o, Object space){
        bytesToList((byte[][]) space);
    }
    /**
     * 将Byte数组转换成ObservableList
     * @param space 磁盘
     * @return list  磁盘的ObservableList形式
     */
    public ObservableList<VirtualDisk> bytesToList(byte[][] space){
        this.list.clear();
        //byte[][] space = disk.getSpace();
        int index =1;
        for (int i=0; i<2; i++){
            for (int j=0; j<space[i].length; j++){
                this.list.add(new VirtualDisk(index++, Math.abs(space[i][j]*64)));
            }
        }
        return this.list;
    }

    /**
     * 初始化virtualDiskTable，并对list设置监听
     */
    public void init(){
        TableColumn<VirtualDisk, Number> numberOfBlock = new TableColumn<VirtualDisk, Number>("块号");
        TableColumn<VirtualDisk, Number> value = new TableColumn<VirtualDisk, Number>("消耗值");
        virtualDiskTable.getColumns().addAll(numberOfBlock, value);
        numberOfBlock.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<VirtualDisk, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<VirtualDisk, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getNumberOfBlock());
                return s;
            }
        });
        value.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<VirtualDisk, Number>, ObservableValue<Number>>() {
            @Override
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<VirtualDisk, Number> param) {
                SimpleIntegerProperty s = new SimpleIntegerProperty(param.getValue().getValue());
                return s;
            }
        });

        list.addListener(new ListChangeListener<VirtualDisk>() {
            @Override
            public void onChanged(Change<? extends VirtualDisk> c) {
                virtualDiskTable.refresh();
            }
        });
    }

    public TableView getVirtualDiskTable() {
        return virtualDiskTable;
    }

    public void setVirtualDiskTable(TableView virtualDiskTable) {
        this.virtualDiskTable = virtualDiskTable;
    }

    public ObservableList<VirtualDisk> getList() {
        return list;
    }

    public void setList(ObservableList<VirtualDisk> list) {
        this.list = list;
    }
}
