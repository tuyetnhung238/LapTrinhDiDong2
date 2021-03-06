package com.ttn.stationarymanagement.presentation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.attention.ShakeAnimator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ttn.stationarymanagement.R;
import com.ttn.stationarymanagement.data.local.WorkWithDb;
import com.ttn.stationarymanagement.data.local.model.NhanVien;
import com.ttn.stationarymanagement.data.local.model.PhongBan;
import com.ttn.stationarymanagement.data.local.model.VaiTro;
import com.ttn.stationarymanagement.presentation.adapter.SelectDepartmentAdapter;
import com.ttn.stationarymanagement.presentation.adapter.SelectRoleAdapter;
import com.ttn.stationarymanagement.presentation.baseview.BaseActivity;
import com.ttn.stationarymanagement.utils.AppUtils;
import com.ttn.stationarymanagement.utils.CustomToast;
import com.ttn.stationarymanagement.utils.GetDataToCommunicate;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddStaftActivity extends BaseActivity {

    public static int REQUEST_ADD_STAFT = 1;
    public static int REQUEST_EDIT_STAFT = 2;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_activity_add_staft_photo)
    ImageView ivPhoto;

    @BindView(R.id.edt_activity_add_staft_name)
    EditText edtNameStaft;

    @BindView(R.id.edt_activity_add_staft_date_of_birth)
    TextView edtDateOfBirth;

    @BindView(R.id.edt_activity_add_staft_phone)
    EditText edtPhone;

    @BindView(R.id.edt_activity_add_staft_email)
    EditText edtEmail;

    @BindView(R.id.rdg_activity_add_staft_gender)
    RadioGroup rdoGender;

    @BindView(R.id.edt_activity_add_staft_note)
    EditText edtNote;

    @BindView(R.id.spn_activity_add_staft_role)
    Spinner spnRole;

    @BindView(R.id.spn_activity_add_staft_department)
    Spinner spDepartment;

    @BindView(R.id.btn_activity_add_staft_add)
    Button btnAdd;

    private String imageStaft = "";
    private List<VaiTro> listVaiTro;
    private int requestSelectPhoto = 1;
    SelectRoleAdapter selectRoleAdapter;
    private List<PhongBan> listPhongBan;
    SelectDepartmentAdapter selectDepartmentAdapter;
    private CompositeDisposable compositeDisposable;

    private long idStaftEdit;
    private boolean isUpload = false;
    private NhanVien nhanVienEdit;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, AddStaftActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staft);
        ButterKnife.bind(this);

        setControls();
        getDataAndSetView();
        setEvents();


    }

    private void getDataAndSetView() {

        // L???y th??ng tin nh??n vi??n c???n c???n nh???t n???u ch???c n??ng c???p nh???t
        if (getIntent().hasExtra("ID_STAFT")) {
            idStaftEdit = getIntent().getLongExtra("ID_STAFT", 0);
            isUpload = true;
        }

        // Thi???t l???p ng??y sinh ng??y hi???n t???i
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        edtDateOfBirth.setText(simpleDateFormat.format(currentDate));

        // L???y th??ng tin nh??n vi??n c???n c???p nh???t
        getAllData();

    }

    // L???y danh s??ch vai tr?? v?? ph??ng ban cho spiner
    private void getAllData() {

        // Ob l???y danh s??ch vai tr??
        Observable<List<VaiTro>> getDataRole = Observable.create(r -> {
            try {
                r.onNext(WorkWithDb.getInstance().getAllRole());
                r.onComplete();
            } catch (Exception e) {
                r.onError(e);
            }

        });

        compositeDisposable.add(getDataRole.subscribeOn(Schedulers.newThread())
                .flatMap(r -> {

                    listVaiTro.addAll(r);    // L??u danh s??ch vai tr?? d?????i c?? s??? d??? li???u

                    // Ob l???y danh s??ch ph??ng ban
                    return Observable.just(WorkWithDb.getInstance().getAllDepartment());

                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {

                    listPhongBan.addAll(r); //  L??u danh s??ch ph??ng ban

                    //  Ki???m tra kh??ng cho ph??p th??m hay s???a n???u danh s??ch ph??ng ban ho???c vai tr?? tr???ng
                    if (listVaiTro.size() < 1 || listPhongBan.size() < 1) {
                        CustomToast.showToastWarning(getApplicationContext(), getResources().getString(R.string.please_setting_role_and_department_in_the_first), Toast.LENGTH_SHORT);
                        finish();
                    }

                    selectRoleAdapter.notifyDataSetChanged();
                    selectDepartmentAdapter.notifyDataSetChanged();

                }, throwable -> {
                    CustomToast.showToastError(getApplicationContext(), getResources().getString(R.string.occurre_error), Toast.LENGTH_SHORT);

                }, () -> {

                    if (isUpload) {     // L???y th??m th??ng tin nh??n vi??n c???n upload n???u ????y l?? s???a nh??n vi??n
                        getInforStaft();
                    }

                }));

    }


    // L???y th??ng tin nh??n vi??n c???n ch???nh s???a
    private void getInforStaft() {

        // Ob l???y nh??n vi??n theo id
        Observable<NhanVien> getStaft = Observable.just(WorkWithDb.getInstance().getStaftById(idStaftEdit));

        getStaft.subscribeOn(Schedulers.newThread())
                .filter(nhanVien -> nhanVien != null)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(nhanVien -> {

            nhanVienEdit = nhanVien; // L??u th??ng tin nh??n vi??n c???n ch???nh s???a

            // L???y ???nh nh??n vi??n
            if (!TextUtils.isEmpty(nhanVien.getAnh())) {

                this.imageStaft = nhanVien.getAnh();

                Picasso.get().load(new File(imageStaft)).error(R.mipmap.app_icon).fit().centerInside().into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            edtNameStaft.setText(nhanVien.getTenNV());      // T??n nh??n vi??n
            edtDateOfBirth.setText(nhanVien.getNgaySinh());     // Ng??y sinh

            if (!TextUtils.isEmpty(nhanVien.getSDT())) {
                edtPhone.setText(nhanVien.getSDT());        // S??? ??i???n tho???i
            }

            if (!TextUtils.isEmpty(nhanVien.getEmail())) {
                edtEmail.setText(nhanVien.getEmail());      // Email
            }

            // Set gi???i t??nh
            if (nhanVien.getGT() == 0) {

                RadioButton rdo = (RadioButton) findViewById(R.id.rdo_male);
                rdo.setChecked(true);


            } else if (nhanVien.getGT() == 1) {

                RadioButton rdo = (RadioButton) findViewById(R.id.rdo_female);
                rdo.setChecked(true);

            } else {

                RadioButton rdo = (RadioButton) findViewById(R.id.rdo_other);
                rdo.setChecked(true);
            }

            // Set v??? tr?? ph??ng ban
            for (int i = 0; i < listPhongBan.size(); i++) {
                if (listPhongBan.get(i).getMaPB() == nhanVien.getMaPB()) {
                    spDepartment.setSelection(i);
                    break;
                }
            }

            // Set v??? tr?? vai tr??
            for (int i = 0; i < listVaiTro.size(); i++) {
                if (listVaiTro.get(i).getMaVT() == nhanVien.getMaVT()) {
                    spnRole.setSelection(i);
                    break;
                }
            }


            if (!TextUtils.isEmpty(nhanVien.getGhiChu())) {
                edtNote.setText(nhanVien.getGhiChu());      // Ghi ch??
            }

            btnAdd.setText(getResources().getString(R.string.upload));
            getSupportActionBar().setTitle(getResources().getString(R.string.edit_staft));
        });


    }


    private void setEvents() {

        // Khi ch???n ???nh
        ivPhoto.setOnClickListener(v -> {

            Intent intent = new Intent();
            // Ch??? ?????nh ki???u file c???n hi???n th???
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            //Hi???n th??? c??c ???ng d???ng c?? th??? x??? l?? ???nh
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_photo)), requestSelectPhoto);

        });

        // Khi ch???n ng??y sinh
        edtDateOfBirth.setOnClickListener(v -> {

            Calendar calendarBirthday = Calendar.getInstance();

            if (!isUpload) {    // N???u l?? th??m nh??n vi??n th?? l???y ng??y hi???n t???i

                int day = calendarBirthday.get(Calendar.DATE);
                int month = calendarBirthday.get(Calendar.MONTH);
                int year = calendarBirthday.get(Calendar.YEAR);

                // Kh???i t???o dialog ch???n ng??y
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calendarBirthday.set(i, i1, i2);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        edtDateOfBirth.setText(simpleDateFormat.format(calendarBirthday.getTime()));
                    }
                }, year, month, day);


                datePickerDialog.show();
            } else {  // N???u l?? c???p nh???t th?? l???y ng??y sinh nh??n vi??n hi???n th??? ????ng v??? tr?? ng??y sinh

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    calendarBirthday.setTime(df.parse(edtDateOfBirth.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int day = calendarBirthday.get(Calendar.DATE);
                int month = calendarBirthday.get(Calendar.MONTH);
                int year = calendarBirthday.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calendarBirthday.set(i, i1, i2);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        edtDateOfBirth.setText(simpleDateFormat.format(calendarBirthday.getTime()));
                    }
                }, year, month, day);

                datePickerDialog.show();
            }

        });


        // Khi nh???n n??t th??m
        btnAdd.setOnClickListener(v -> {

            // Ki???m tra t??n nh??n vi??n
            if (TextUtils.isEmpty(edtNameStaft.getText().toString())) {

                new ShakeAnimator().setDuration(700).setRepeatTimes(0).setTarget(edtNameStaft).start();
                edtNameStaft.setError(getResources().getString(R.string.name_staft_do_not_empty));
                edtNameStaft.requestFocus();
                return;
            }

            // Ki???m tra email
            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                new ShakeAnimator().setDuration(700).setRepeatTimes(0).setTarget(edtEmail).start();
                edtEmail.setError(getResources().getString(R.string.email_do_not_empty));
                edtEmail.requestFocus();
                return;
            }

            // Ki???m tra c???u tr??c email
            if (!AppUtils.checkValidEmail(edtEmail.getText().toString())) {
                new ShakeAnimator().setDuration(700).setRepeatTimes(0).setTarget(edtEmail).start();
                edtEmail.setError(getResources().getString(R.string.email_format_invalid));
                edtEmail.requestFocus();
                return;
            }

            if (isUpload) {     // C???p nh???t th??ng tin nh??n vi??n
                uploadStaft();
            } else {        // Th??m nh??n vi??n
                createNewStaft();
            }


        });


    }

    // C???p nh???t th??ng tin nh??n vi??n
    private void uploadStaft() {

        nhanVienEdit.setAnh(!TextUtils.isEmpty(imageStaft) ? imageStaft : "");      // C???p nh???t ???nh
        nhanVienEdit.setTenNV(edtNameStaft.getText().toString());       // C???p nh???t t??n
        nhanVienEdit.setNgaySinh(edtDateOfBirth.getText().toString());       //  C???p nh???t ng??y sinh
        nhanVienEdit.setSDT(GetDataToCommunicate.convertStringToString(edtPhone.getText().toString()));     // C???p nh???t s??? ??i???n tho???i
        nhanVienEdit.setEmail(GetDataToCommunicate.convertStringToString(edtEmail.getText().toString()));       // C???p nh???t email

        // C???p nh???t gi???i t??nh
        switch (rdoGender.getCheckedRadioButtonId()) {
            case R.id.rdo_male: //Nam
                nhanVienEdit.setGT(0);
                break;
            case R.id.rdo_female: // Nu
                nhanVienEdit.setGT(1);
                break;
            case R.id.rdo_other:    // Khac
                nhanVienEdit.setGT(2);
                break;

        }

        // C???p nh???t vai tr?? nh??n vi??n
        if (listVaiTro.size() > 0) {
            VaiTro vaiTro = listVaiTro.get(spnRole.getSelectedItemPosition());
            nhanVienEdit.setMaVT(vaiTro.getMaVT());
        }

        // C???p nh???t ph??ng ban
        if (listPhongBan.size() > 0) {
            PhongBan phongBan = listPhongBan.get(spDepartment.getSelectedItemPosition());
            nhanVienEdit.setMaPB(phongBan.getMaPB());
        }

        // C???p nh???t ghi ch??
        nhanVienEdit.setGhiChu(!TextUtils.isEmpty(edtNote.getText().toString()) ? edtNote.getText().toString() : "");

        // Ob c???p nh???t th??ng tin nh??n vi??n
        Observable<Boolean> updateStaft = Observable.just(WorkWithDb.getInstance().update(nhanVienEdit));

        compositeDisposable.add(updateStaft.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {

                    if (aBoolean) {     // C???p nh???t th??nh c??ng
                        CustomToast.showToastSuccesstion(this, getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT);

                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();

                    } else {
                        CustomToast.showToastError(this, getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT);
                    }
                }, throwable -> {
                    CustomToast.showToastError(this, getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT);
                }, () -> {

                }));

    }

    // T???o m???i nh??n vi??n
    private void createNewStaft() {

        NhanVien nhanVien = new NhanVien();

        nhanVien.setAnh(!TextUtils.isEmpty(imageStaft) ? imageStaft : ""); // ???nh nh??n vi??n
        nhanVien.setTenNV(edtNameStaft.getText().toString());               // T??n nh??n vi??n
        nhanVien.setNgaySinh(edtDateOfBirth.getText().toString());          // Ng??y sinh
        nhanVien.setSDT(!TextUtils.isEmpty(edtPhone.getText().toString()) ? edtPhone.getText().toString() : "");    // S??? ??i???n tho???i
        nhanVien.setEmail(GetDataToCommunicate.convertStringToString(edtEmail.getText().toString()));           // Email
        nhanVien.setNgayTao(GetDataToCommunicate.getCurrentDate());     // Ng??y t???o

        // Gi???i t??nh
        switch (rdoGender.getCheckedRadioButtonId()) {
            case R.id.rdo_male: //Nam
                nhanVien.setGT(0);
                break;
            case R.id.rdo_female: // Nu
                nhanVien.setGT(1);
                break;
            case R.id.rdo_other:    // Khac
                nhanVien.setGT(2);
                break;
        }


        // Vai tr??
        if (listVaiTro.size() > 0) {
            VaiTro vaiTro = listVaiTro.get(spnRole.getSelectedItemPosition());
            nhanVien.setMaVT(vaiTro.getMaVT());
        }

        // Ph??ng ban
        if (listPhongBan.size() > 0) {
            PhongBan phongBan = listPhongBan.get(spDepartment.getSelectedItemPosition());
            nhanVien.setMaPB(phongBan.getMaPB());
        }

        // Ghi ch??
        nhanVien.setGhiChu(!TextUtils.isEmpty(edtNote.getText().toString()) ? edtNote.getText().toString() : "");

        // Ob t???o nh??n vi??n
        Observable<Boolean> createStaft = Observable.create(r -> {
            try {
                r.onNext(WorkWithDb.getInstance().insert(nhanVien));
                r.onComplete();
            } catch (Exception e) {
                r.onError(e);
            }

        });

        compositeDisposable.add(createStaft.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(r -> {
                    if (r) {
                        CustomToast.showToastSuccesstion(getApplicationContext(), getResources().getString(R.string.add_successful), Toast.LENGTH_SHORT);
                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();

                    } else {
                        CustomToast.showToastError(getApplicationContext(), getResources().getString(R.string.add_failed), Toast.LENGTH_SHORT);
                    }

                }, throwable -> {
                    CustomToast.showToastError(getApplicationContext(), getResources().getString(R.string.add_failed), Toast.LENGTH_SHORT);
                }));

    }

    private void setControls() {

        compositeDisposable = new CompositeDisposable();

        // Kh???i t???o toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.add_staft));

        // Kh???i t???o danh vai tr?? v?? adapter qu???n l??
        listVaiTro = new ArrayList();
        selectRoleAdapter = new SelectRoleAdapter(this, listVaiTro);
        spnRole.setAdapter(selectRoleAdapter);

        // Kh???i t???o danh s??ch ph??ng ban v?? adapter qu???n l??
        listPhongBan = new ArrayList();
        selectDepartmentAdapter = new SelectDepartmentAdapter(this, listPhongBan);
        spDepartment.setAdapter(selectDepartmentAdapter);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        // Nh???n k???t qu??? ch???n ???nh t??? th?? vi???n
        if (requestCode == requestSelectPhoto && resultCode == RESULT_OK) {

            // L???y ?????a ch??? h??nh ???nh
            Uri uri = data.getData();

            // L??u l???i ?????a ch??? h??nh ???nh ???????c ch???n
            String imagePath = getPath(uri);

            if (!TextUtils.isEmpty(imagePath)) {

                this.imageStaft = imagePath;

                Picasso.get().load(new File(imagePath)).error(R.mipmap.app_icon).fit().centerInside().into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                CustomToast.showToastError(getApplicationContext(), getResources().getString(R.string.select_photo_fail), Toast.LENGTH_SHORT);
            }

        }

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}