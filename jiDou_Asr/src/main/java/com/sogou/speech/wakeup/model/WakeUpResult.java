package com.sogou.speech.wakeup.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class WakeUpResult {
	public WakeUpResult(String aWord, float aConfid, float aLike){
		word = aWord;
		confid = aConfid;
		like = aLike;
	}
	
	@SerializedName("word")
	public String word = null;

	@SerializedName("confid")
	public float confid = 0;

	@SerializedName("like")
	public float like = 0;
	
	@Override
	public final String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}
