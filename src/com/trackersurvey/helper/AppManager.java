package com.trackersurvey.helper;

import java.util.Iterator;
import java.util.Stack;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
/**
* @��Ŀ����: <br>
* @������: ��ջ����<br>
* @������  <br>
* @����ʱ��  <br>
* @�޸���  <br>
* @�޸�ʱ�� 2015��10��12�� ����2:00:40 <br>
 */
public class AppManager {

	private static Stack<Activity> activityStack;
	private static AppManager instance;

	private AppManager() {
	}

	/**
	 * ��һʵ��
	 */
	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}

	/**
	 * ���Activity����ջ
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * ��ȡ��ǰActivity����ջ�����һ��ѹ��ģ�
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * ������ǰActivity����ջ�����һ��ѹ��ģ�
	 
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}
	 */
	/**
	 * ����ָ����Activity
	 
	public void finishActivity(Activity activity) {
		if (activity != null) {
			
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}
	 */
	/**
	 * ����ָ��������Activity
	 */
	public void finishActivity(Class<?> cls) {
		/**����ɾ���ᱨ�쳣��ConcurrentModificationException
		 * for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}*/
		Iterator<Activity> iter= activityStack.iterator();
		while(iter.hasNext()){
			Activity activity=(Activity)iter.next();
			if(activity.getClass().equals(cls)){
				iter.remove();
				activity.finish();
				break;
			}
		}
	}

	/**
	 * ��������Activity
	 */
	public void finishAllActivity() {
		for (int i = 0, size = activityStack.size(); i < size; i++) {
			if (null != activityStack.get(i)) {
				activityStack.get(i).finish();
			}
		}
		activityStack.clear();
	}

	/**
	 * �������˵�ǰҳ��������Activity
	 */
	public void finishOtherAllActivity() {
		for (int i = 0, size = activityStack.size(); i < size-1; i++) {
			if (null != activityStack.get(i)) {
				activityStack.get(i).finish();
			}
		}
		// activityStack.clear();
	}
	public void finishOtherActivity() {
//		for (int i = 1, size = activityStack.size(); i < size; i++) {
//			if (null != activityStack.get(i)) {
				activityStack.get(activityStack.size()-1).finish();
				activityStack.get(activityStack.size()-2).finish();
//			}
//		}
		// activityStack.clear();
	}

	/**
	 * �˳�Ӧ�ó���
	 */
	public void AppExit(Context context) {
		try {
			finishAllActivity();
			ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			activityMgr.killBackgroundProcesses(context.getPackageName());
			System.exit(0);
			android.os.Process.killProcess(android.os.Process.myPid());

		} catch (Exception e) {
		}
	}
}
