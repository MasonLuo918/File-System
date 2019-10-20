package com.left_interface;

import com.system.model.Disk;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.Observable;
import java.util.Observer;

/**
 * @author QTJ
 */
public class VirtualDiskTable implements Observer {
    //���̱�
    private TableView virtualDiskTable ;
    //��Ŵ���ʹ������ı��
    private ObservableList<VirtualDisk> list;
    private Disk disk = Disk.getInstance();

    /**
    @param scene
     */
    public VirtualDiskTable(Scene scene){
        //����һ��ObservableListʵ��
        this.list = FXCollections.observableArrayList();
        //��list��Ϊ����Դ����һ��TableViewʵ��
        this.virtualDiskTable = new TableView(this.list);
        virtualDiskTable.prefHeightProperty().bind(scene.heightProperty().multiply(0.6));
        virtualDiskTable.prefWidthProperty().bind(scene.widthProperty().multiply(0.25));
        init();
    }

    /**
     * �����۲��߸Ķ�ʱ���÷����ᱻ����
     * @param o  ���۲����
     * @param space ���۲���󴫻����Ĵ���
     */
    @Override
    public void update(Observable o, Object space){
        bytesToList((byte[][]) space);
    }
    /**
     * ��Byte����ת����ObservableList
     * @param space ����
     * @return list  ���̵�ObservableList��ʽ
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
     * ��ʼ��virtualDiskTable������list���ü���
     */
    public void init(){
        TableColumn<VirtualDisk, Number> numberOfBlock = new TableColumn<VirtualDisk, Number>("���");
        TableColumn<VirtualDisk, Number> value = new TableColumn<VirtualDisk, Number>("����ֵ");

        virtualDiskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        virtualDiskTable.getColumns().addAll(numberOfBlock, value);

        //�����������
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
