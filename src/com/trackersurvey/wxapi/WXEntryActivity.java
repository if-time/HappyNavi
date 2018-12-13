package com.trackersurvey.wxapi;


import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.trackersurvey.helper.ShareToWeChat;

import android.app.Activity;
import android.util.Log;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	private  IWXAPI api = ShareToWeChat.api;

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub
		api.handleIntent(getIntent(), this);
		Log.i("Eaa_wx_result",arg0.toString());
	}

	@Override
	public void onResp(BaseResp arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}