package jc.sky.core.plugin;

import android.os.Bundle;
import android.support.annotation.NonNull;

import jc.sky.view.SKYActivity;
import jc.sky.view.SKYBuilder;

/**
 * @创建人 sky
 * @创建时间 16/1/6
 * @类描述 activity拦截器
 */
public interface SKYActivityInterceptor {

	void build(SKYBuilder initialSKYBuilder);

	void onCreate(SKYActivity SKYIView, Bundle bundle, Bundle savedInstanceState);

	void onStart(SKYActivity SKYIView);

	void onResume(SKYActivity SKYIView);

	void onPause(SKYActivity SKYIView);

	void onStop(SKYActivity SKYIView);

	void onDestroy(SKYActivity SKYIView);

	void onRestart(SKYActivity SKYIView);

	void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

	SKYActivityInterceptor NONE	= new SKYActivityInterceptor() {

										@Override public void build(SKYBuilder initialSKYBuilder) {

										}

										@Override public void onCreate(SKYActivity SKYIView, Bundle bundle, Bundle savedInstanceState) {

										}

										@Override public void onStart(SKYActivity SKYIView) {

										}

										@Override public void onResume(SKYActivity SKYIView) {

										}

										@Override public void onPause(SKYActivity SKYIView) {

										}

										@Override public void onStop(SKYActivity SKYIView) {

										}

										@Override public void onDestroy(SKYActivity SKYIView) {

										}

										@Override public void onRestart(SKYActivity SKYIView) {

										}

										@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

										}
									};

}
