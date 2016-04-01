package com.sogou.speech.view;


import com.sogou.speech.wakeupclient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
	   
public final class Title extends LinearLayout {   	   
	    public LinearLayout backLayout;
	    public TextView titleTV;
	    
	    public Title(Context context) {   
	  	        this(context, null);   
	    }   
	    public Title(Context context, AttributeSet attrs) {   
	    	super(context, attrs);   
	        LayoutInflater.from(context).inflate(R.layout.title, this, true);   
	        backLayout = (LinearLayout) findViewById(R.id.back_layout);  
	        titleTV = (TextView)this.findViewById(R.id.title_tv);
	    }   	    
}


