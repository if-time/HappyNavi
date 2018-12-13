/**
 * 
 */
package com.trackersurvey.helper;

import android.widget.GridView;
import android.content.Context;
import android.util.AttributeSet;
 
/**
 * �Զ���ġ��Ź��񡱡���������ʾ���������ͼƬ���� ��������⣺GridView��ʾ��ȫ��ֻ��ʾ��һ�е�ͼƬ���Ƚ���֣�������дGridView�����
 * 
 * @author Eaa
 * @version 2015��12��11��  ����2:59:55
 * 
 */
public class NoScrollGridView extends GridView {
 
    public NoScrollGridView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
 
    public NoScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
 
    public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
    
    
 
}