package com.trackersurvey.helper;
	
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
	
public class MyWebChromeClient extends WebChromeClient {
	
	@Override
	public void onCloseWindow(WebView window) {
		super.onCloseWindow(window);
	}
	
	@Override
	public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
		return super.onCreateWindow(view, dialog, userGesture, resultMsg);
	}
	
	/**
	* ����Ĭ�ϵ�window.alertչʾ���棬����title����ʾΪ��������file:////��
	*/
	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		builder.setTitle("��ʾ��").setMessage(message).setPositiveButton("ȷ��", null);
		//����Ҫ�󶨰����¼�
		//����keycode����84֮��İ���
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,KeyEvent event) {
				Log.v("onJsAlert", "keyCode==" + keyCode + "event=" + event);
				return true;
			}
		});
		//��ֹ��Ӧ��back�����¼�
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		result.confirm();//��Ϊû�а��¼�����Ҫǿ��confirm,����ҳ�������ʾ�������ݡ�
		return true;
		// return super.onJsAlert(view, url, message, result);
	}
	
	@Override
	public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
		return super.onJsBeforeUnload(view, url, message, result);
	}
	
	/**
	* ����Ĭ�ϵ�window.confirmչʾ���棬����title����ʾΪ��������file:////��
	*/
	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		builder.setTitle("��ʾ").setMessage(message).setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		}).setNeutralButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				result.cancel();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				result.cancel();
			}
		});
	
		//����keycode����84֮��İ��������ⰴ�����¶Ի�����Ϣ��ҳ���޷��ٵ����Ի��������
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				Log.v("onJsConfirm", "keyCode==" + keyCode + "event="+ event);
				return true;
			}
		});
		//��ֹ��Ӧ��back�����¼�
		//builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
		// return super.onJsConfirm(view, url, message, result);
	}
	
	/**
	* ����Ĭ�ϵ�window.promptչʾ���棬����title����ʾΪ��������file:////��
	* window.prompt(������������������ַ��, ��618119.com��);
	*/
	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
			final JsPromptResult result) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		builder.setTitle("��ʾ").setMessage(message);
		final EditText et = new EditText(view.getContext());
		et.setSingleLine();
		et.setText(defaultValue);
		builder.setView(et);
		builder.setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				result.confirm(et.getText().toString());
			}
		}).setNeutralButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				result.cancel();
			}
		});
	
		//����keycode����84֮��İ��������ⰴ�����¶Ի�����Ϣ��ҳ���޷��ٵ����Ի��������
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				Log.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event);
				return true;
			}
		});
	
		//��ֹ��Ӧ��back�����¼�
		//builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
		// return super.onJsPrompt(view, url, message, defaultValue, result);
	}
	
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		super.onProgressChanged(view, newProgress);
	}
	
	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		super.onReceivedIcon(view, icon);
	}
	
	@Override
	public void onReceivedTitle(WebView view, String title) {
		super.onReceivedTitle(view, title);
	}
	
	@Override
	public void onRequestFocus(WebView view) {
		super.onRequestFocus(view);
	}
	
}
