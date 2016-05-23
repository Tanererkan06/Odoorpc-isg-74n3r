package com.odoo.addons.Risk;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.Risk.models.RiskAsessmentmodels;

import com.odoo.addons.Risk.utils.ShareUtil;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import odoo.controls.OField;
import odoo.controls.OForm;
import odoo.helper.OdooFields;
import odoo.helper.utils.gson.OdooResult;

public class RiskassesmentDetails extends OdooCompatActivity
        implements View.OnClickListener, OField.IOnFieldValueChangeListener {
    public static final String TAG = RiskassesmentDetails.class.getSimpleName();
    public static String KEY_PARTNER_TYPE = "partner_type";
    private final String KEY_MODE = "key_edit_mode";
    private final String KEY_NEW_IMAGE = "key_new_image";
    private ActionBar actionBar;
    private Bundle extras;
    private RiskAsessmentmodels resPartner;
    private ODataRow record = null;
    private ParallaxScrollView parallaxScrollView;
    private ImageView userImage = null, captureImage = null;
    private TextView mTitleView = null;
    private OForm mForm;
    private App app;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private OFileManager fileManager;
    private String newImage = null;
    private RiskAsessment.Type partnerType = RiskAsessment.Type.Customer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.riskassesment_detail);
        OAppBarUtils.setAppBar(this, false);
        fileManager = new OFileManager(this);
        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("");
        if (savedInstanceState != null) {
            mEditMode = savedInstanceState.getBoolean(KEY_MODE);
            newImage = savedInstanceState.getString(KEY_NEW_IMAGE);
        }
        app = (App) getApplicationContext();
        parallaxScrollView = (ParallaxScrollView) findViewById(R.id.parallaxScrollView);
        parallaxScrollView.setActionBar(actionBar);
        userImage = (ImageView) findViewById(android.R.id.icon);
        mTitleView = (TextView) findViewById(android.R.id.title);
        resPartner = new RiskAsessmentmodels(this, null);
        extras = getIntent().getExtras();
        if (extras != null)
            partnerType = RiskAsessment.Type.valueOf(extras.getString(KEY_PARTNER_TYPE));
        if (!hasRecordInExtra())
            mEditMode = true;
        setupActionBar();
    }

    private boolean hasRecordInExtra() {
        return extras != null && extras.containsKey(OColumn.ROW_ID);
    }

    private void setMode(Boolean edit) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_risk_detail_more).setVisible(!edit);
            mMenu.findItem(R.id.menu_risk_edit).setVisible(!edit);
            mMenu.findItem(R.id.menu_risk_save).setVisible(edit);
            mMenu.findItem(R.id.menu_risk_cancel).setVisible(edit);
        }
        int color = Color.DKGRAY;
        if (record != null) {
            color = OStringColorUtil.getStringColor(this, record.getString("name"));
        }
        if (edit) {
            if (hasRecordInExtra()) {
                actionBar.setTitle(R.string.label_edit);
            } else
                actionBar.setTitle(R.string.label_new);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            mForm = (OForm) findViewById(R.id.riskFormEdit);
            captureImage = (ImageView) findViewById(R.id.captureImage);
            captureImage.setOnClickListener(this);
            userImage = (ImageView) findViewById(android.R.id.icon1);
            findViewById(R.id.parallaxScrollView).setVisibility(View.GONE);
            findViewById(R.id.riskScrollViewEdit).setVisibility(View.VISIBLE);
            OField is_company = (OField) findViewById(R.id.is_company_edit);
            is_company.setOnValueChangeListener(this);
        } else {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_shade));
            userImage = (ImageView) findViewById(android.R.id.icon);
            mForm = (OForm) findViewById(R.id.riskForm);
            findViewById(R.id.riskScrollViewEdit).setVisibility(View.GONE);
            findViewById(R.id.parallaxScrollView).setVisibility(View.VISIBLE);
        }
        setColor(color);
    }

    private void setupActionBar() {
        if (!hasRecordInExtra()) {
            setMode(mEditMode);
            userImage.setColorFilter(Color.parseColor("#ffffff"));
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = resPartner.browse(rowId);
           // record.put("full_address", resPartner.getAddress(record));
            checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
            mTitleView.setText(record.getString("name"));
            setriskImage();
            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
                BigImageLoader bigImageLoader = new BigImageLoader();
                bigImageLoader.execute(record.getInt("id"));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.full_address:
                IntentUtils.redirectToMap(this, record.getString("full_address"));
                break;
            case R.id.website:
                IntentUtils.openURLInBrowser(this, record.getString("website"));
                break;
            case R.id.email:
                IntentUtils.requestMessage(this, record.getString("email"));
                break;
            case R.id.phone_number:
                IntentUtils.requestCall(this, record.getString("phone"));
                break;
            case R.id.mobile_number:
                IntentUtils.requestCall(this, record.getString("mobile"));
                break;
            case R.id.captureImage:
                fileManager.requestForFile(OFileManager.RequestType.IMAGE_OR_CAPTURE_IMAGE);
                break;
        }
    }

    private void checkControls() {
        findViewById(R.id.full_address).setOnClickListener(this);
        findViewById(R.id.website).setOnClickListener(this);
        findViewById(R.id.email).setOnClickListener(this);
        findViewById(R.id.phone_number).setOnClickListener(this);
        findViewById(R.id.mobile_number).setOnClickListener(this);
    }

    private void setriskImage() {
        if (!record.getString("image_small").equals("false")) {
            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            userImage.setColorFilter(null);
            String base64 = newImage;
            if (newImage == null) {
                if (!record.getString("large_image").equals("false")) {
                    base64 = record.getString("large_image");
                } else {
                    base64 = record.getString("image_small");
                }
            }
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, base64));
        } else {
            userImage.setColorFilter(Color.parseColor("#ffffff"));
        }
    }

    private void setColor(int color) {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.parallax_view);
        frameLayout.setBackgroundColor(color);
        parallaxScrollView.setParallaxOverLayColor(color);
        parallaxScrollView.setBackgroundColor(color);
        mForm.setIconTintColor(color);
        findViewById(R.id.parallax_view).setBackgroundColor(color);
        findViewById(R.id.parallax_view_edit).setBackgroundColor(color);
        findViewById(R.id.riskScrollViewEdit).setBackgroundColor(color);
        if (captureImage != null) {
            GradientDrawable shapeDrawable =
                    (GradientDrawable) getResources().getDrawable(R.drawable.circle_mask_primary);
            shapeDrawable.setColor(color);
            captureImage.setBackgroundDrawable(shapeDrawable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_risk_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    switch (partnerType) {
                        case Supplier:
                            values.put("customer", "false");
                            values.put("supplier", "true");
                            break;
                        default:
                            values.put("customer", "true");
                            break;
                    }
                    if (newImage != null) {
                        values.put("image_small", newImage);
                        values.put("large_image", newImage);
                    }
                    if (record != null) {
                        resPartner.update(record.getInt(OColumn.ROW_ID), values);
                        Toast.makeText(this, R.string.toast_information_saved, Toast.LENGTH_LONG).show();
                        mEditMode = !mEditMode;
                        setupActionBar();
                    } else {
                        final int row_id = resPartner.insert(values);
                        if (row_id != OModel.INVALID_ROW_ID) {
                            finish();
                        }
                    }
                }
                break;
            case R.id.menu_risk_cancel:
                if (record == null) {
                    finish();
                    return true;
                }
            case R.id.menu_risk_edit:
                mEditMode = !mEditMode;
                setMode(mEditMode);
                mForm.setEditable(mEditMode);
                mForm.initForm(record);
                setriskImage();
                break;
            case R.id.menu_risk_share:
                ShareUtil.shareContact(this, record, true);
                break;
            case R.id.menu_risk_import:
                ShareUtil.shareContact(this, record, false);
                break;
            case R.id.menu_risk_delete:
                OAlert.showConfirm(this, OResource.string(this,
                        R.string.confirm_are_you_sure_want_to_delete),
                        new OAlert.OnAlertConfirmListener() {
                            @Override
                            public void onConfirmChoiceSelect(OAlert.ConfirmType type) {
                                if (type == OAlert.ConfirmType.POSITIVE) {
                                    // Deleting record and finishing activity if success.
                                    if (resPartner.delete(record.getInt(OColumn.ROW_ID))) {
                                        Toast.makeText(RiskassesmentDetails.this, R.string.toast_record_deleted,
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                        });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_riskassetment_detail, menu);
        mMenu = menu;
        setMode(mEditMode);
        return true;
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {
        if (field.getFieldName().equals("is_company")) {
            Boolean checked = Boolean.parseBoolean(value.toString());
            int view = (checked) ? View.GONE : View.VISIBLE;
            findViewById(R.id.parent_id).setVisibility(view);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_MODE, mEditMode);
        outState.putString(KEY_NEW_IMAGE, newImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OValues values = fileManager.handleResult(requestCode, resultCode, data);
        if (values != null && !values.contains("size_limit_exceed")) {
            newImage = values.getString("datas");
            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            userImage.setColorFilter(null);
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, newImage));
        } else if (values != null) {
            Toast.makeText(this, R.string.toast_image_size_too_large, Toast.LENGTH_LONG).show();
        }
    }

    private class BigImageLoader extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String image = null;
            try {
                Thread.sleep(300);
                OdooFields fields = new OdooFields();
                fields.addAll(new String[]{"image_medium"});
                OdooResult record = resPartner.getServerDataHelper().read(null, params[0]);
                if (record != null && !record.getString("image_medium").equals("false")) {
                    image = record.getString("image_medium");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                if (!result.equals("false")) {
                    OValues values = new OValues();
                    values.put("large_image", result);
                    resPartner.update(record.getInt(OColumn.ROW_ID), values);
                    record.put("large_image", result);
                    setriskImage();
                }
            }
        }
    }
}