package yangtse.henu.userheadimageset.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import yangtse.henu.userheadimageset.CropOption;
import yangtse.henu.userheadimageset.CropOptionAdapter;
import yangtse.henu.userheadimageset.MyApplication;
import yangtse.henu.userheadimageset.R;

/**
 * Created by Yangtse on 2017/10/21.
 */

public class BottomMenuDialog extends Dialog implements OnClickListener {

    private Button photographBtn;
    private Button localPhotosBtn;
    private Button cancelBtn;
    private Context context;

    private View.OnClickListener confirmListener;
    private View.OnClickListener cancelListener;
    private View.OnClickListener middleListener;

    private String confirmText;
    private String middleText;
    private String cancelText;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private Uri imgUri;
    private Activity activity;

    /**
     * @param context
     */
    public BottomMenuDialog(Context context) {
        super(context,R.style.dialogFullscreen);
        this.context=context;
        activity=(Activity)context;
    }

    /**
     * @param context
     * @param theme
     */
    public BottomMenuDialog(Context context, int theme) {
        super(context, theme);
    }

    public BottomMenuDialog(Context context, String confirmText, String middleText) {
        super(context, R.style.dialogFullscreen);
        this.confirmText = confirmText;
        this.middleText = middleText;
    }
    public BottomMenuDialog(Context context, String confirmText, String middleText, String cancelText) {
        super(context, R.style.dialogFullscreen);
        this.confirmText = confirmText;
        this.middleText = middleText;
        this.cancelText = cancelText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_bottom);
        Window window=getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.5f;
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(layoutParams);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        photographBtn = (Button) findViewById(R.id.photographBtn);
        localPhotosBtn = (Button) findViewById(R.id.localPhotosBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        if (!TextUtils.isEmpty(confirmText)) {
            photographBtn.setText(confirmText);
        }
        if (!TextUtils.isEmpty(middleText)) {
            localPhotosBtn.setText(middleText);
        }
        if (!TextUtils.isEmpty(cancelText)) {
            cancelBtn.setText(cancelText);
        }
        cancelBtn.setOnClickListener(this);
        photographBtn.setOnClickListener(this);
        localPhotosBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.photographBtn) {
            /*if (confirmListener != null) {
                confirmListener.onClick(v);
            }
            Toast.makeText(MyApplication.getContext(),"hello",Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            imgUri = Uri.fromFile(new File(Environment
                    .getExternalStorageDirectory(), "avatar_"
                    + String.valueOf(System.currentTimeMillis())
                    + ".png"));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            activity.startActivityForResult(intent, PICK_FROM_CAMERA);
            doCrop();
            return;
        }
        if (id == R.id.localPhotosBtn) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            activity.startActivityForResult(intent,PICK_FROM_FILE);
            return;
        }
        if (id == R.id.cancelBtn) {
            if (cancelListener != null) {
                cancelListener.onClick(v);
            }
            dismiss();
            return;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        dismiss();
        return true;
    }
    public View.OnClickListener getConfirmListener() {
        return confirmListener;
    }

    public void setConfirmListener(View.OnClickListener confirmListener) {
        this.confirmListener = confirmListener;
    }
    public View.OnClickListener getCancelListener() {
        return cancelListener;
    }

    public void setCancelListener(View.OnClickListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public View.OnClickListener getMiddleListener() {
        return middleListener;
    }

    public void setMiddleListener(View.OnClickListener middleListener) {
        this.middleListener = middleListener;
    }
    private void doCrop() {

        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(
                intent, 0);
        int size = list.size();

        if (size == 0) {
            Toast.makeText(context, "can't find crop app", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            intent.setData(imgUri);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            // only one
            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName,
                        res.activityInfo.name));
                activity.startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                // many crop app
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = activity.getPackageManager().getApplicationLabel(
                            res.activityInfo.applicationInfo);
                    co.icon = activity.getPackageManager().getApplicationIcon(
                            res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent
                            .setComponent(new ComponentName(
                                    res.activityInfo.packageName,
                                    res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(
                        activity.getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("choose a app");
                builder.setAdapter(adapter,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                activity.startActivityForResult(
                                        cropOptions.get(item).appIntent,
                                        CROP_FROM_CAMERA);
                            }
                        });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (imgUri != null) {
                            activity.getContentResolver().delete(imgUri, null, null);
                            imgUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
