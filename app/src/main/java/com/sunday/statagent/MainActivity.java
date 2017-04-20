package com.sunday.statagent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {

	private Context context;
	private Button btn1, btn2;
	private EditText et_memberId, et_area_code, et_track_type, et_page_id,
			et_page_col, et_col_position, et_col_pos_content, et_entry_method,
			et_url;
	private String memberId = "rO0ABXQAD1YawoQ8w5h2QsK8ekYrOA==", area_code, track_type, page_id, page_col,
			col_position, col_pos_content, entry_method, url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		// 参数含义：1、context,上下文。2、应用类型,参考excel表。3、是否为debug模式，debug模式可以打印部分日志，true未开启，false为关闭
		StatAgent.init(context, "2", true);
		initView();
	}

	private void initView() {
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		et_memberId = (EditText) findViewById(R.id.et_memberId);
		et_area_code = (EditText) findViewById(R.id.et_area_code);
		et_track_type = (EditText) findViewById(R.id.et_track_type);
		et_page_id = (EditText) findViewById(R.id.et_page_id);
		et_page_col = (EditText) findViewById(R.id.et_page_col);
		et_col_position = (EditText) findViewById(R.id.et_col_position);
		et_col_pos_content = (EditText) findViewById(R.id.et_col_pos_content);
		et_entry_method = (EditText) findViewById(R.id.et_entry_method);
		et_url = (EditText) findViewById(R.id.et_url);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
	}

	/*
	 * 必须添加此方法
	 */
	@Override
	protected void onPause() {
		super.onPause();
		StatAgent.stopWatcher(context);
	}

	/*
	 * 必须添加此方法
	 */
	@Override
	protected void onResume() {
		super.onResume();
		StatAgent.initWatcher(context);
	}

	/*
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn1:
			memberId = et_memberId.getText().toString().trim();
			StatAgent.initMemberId(context, memberId);
			break;
		case R.id.btn2:
			getString();
			StatAgent.initAction(context, area_code, track_type, page_id,
					page_col, col_position, col_pos_content, entry_method, url,"10");
			break;
		default:
			break;
		}
	}

	private void getString() {
		area_code = et_area_code.getText().toString().trim();
		track_type = et_track_type.getText().toString().trim();
		page_id = et_page_id.getText().toString().trim();
		page_col = et_page_col.getText().toString().trim();
		col_position = et_col_position.getText().toString().trim();
		col_pos_content = et_col_pos_content.getText().toString().trim();
		entry_method = et_entry_method.getText().toString().trim();
		url = et_url.getText().toString().trim();
	}
}
