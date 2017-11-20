package com.tunaemre.remotecontroller.operator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionOperator
{
	public static int REQUEST_CAMERA_PERMISSION = 9000;

	public boolean isCameraPermissionGranded(Context context)
	{
		if (Build.VERSION.SDK_INT < 23) return true;

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) return true;

		return false;
	}

	public boolean requestCameraPermission(Activity context)
	{
		if (isCameraPermissionGranded(context)) return true;

		ActivityCompat.requestPermissions(context, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

		return true;
	}
}
