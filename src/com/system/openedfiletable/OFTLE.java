package com.system.openedfiletable;

/*
 * �Ѵ��ļ��������Ͷ���
 */
public class OFTLE {
	private String name; //�ļ�����·����
	private char attribute; //�ļ������ԣ���1���ֽڱ�ʾ
	private int number; //�ļ���ʼ�̿��
	private int length; //�ļ����ȣ��ļ�ռ�õ��ֽ���
	private int flag; //�������ͣ��á�0����ʾ�Զ�������ʽ���ļ����á�1����ʾ��д������ʽ���ļ�
	private Pointer read = new Pointer(); //���ļ���λ�ã��ļ���ʱdnumΪ�ļ���ʼ�̿�ţ�bnumΪ��0��
	private Pointer write = new Pointer(); //д�ļ���λ�ã��ļ��ս���ʱdnumΪ�ļ���ʼ�̿�ţ�bnumΪ��0�������ļ�ʱdnum��bnumΪ�ļ���ĩβλ��
	
	public OFTLE(String name, Boolean attribute, int number) {
		this.name = name;
		if(attribute == true) {
			this.attribute = '1';
		}else {
			this.attribute = '0';
		}
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public char getAttribute() {
		return attribute;
	}

	public void setAttribute(char attribute) {
		this.attribute = attribute;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public Pointer getRead() {
		return read;
	}

	public void setRead(Pointer read) {
		this.read = read;
	}

	public Pointer getWrite() {
		return write;
	}

	public void setWrite(Pointer write) {
		this.write = write;
	}

}
