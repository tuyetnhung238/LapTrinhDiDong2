package com.ttn.stationarymanagement.presentation.dialog_fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.rey.material.widget.ProgressView;
import com.ttn.stationarymanagement.R;
import com.ttn.stationarymanagement.data.local.WorkWithDb;
import com.ttn.stationarymanagement.data.local.model.CapPhat;
import com.ttn.stationarymanagement.data.local.model.NhanVien;
import com.ttn.stationarymanagement.data.local.model.VanPhongPham;
import com.ttn.stationarymanagement.presentation.activity.DetailBillActivity;
import com.ttn.stationarymanagement.presentation.adapter.GroupBillAdapter;
import com.ttn.stationarymanagement.presentation.baseview.FullScreenDialog;
import com.ttn.stationarymanagement.presentation.model.GroupBillModel;
import com.ttn.stationarymanagement.utils.CustomToast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableConverter;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchDialogFragment extends FullScreenDialog {

    private List<String> listDataSugestion;
    private ArrayAdapter<String> adapterSugestion;
    private CompositeDisposable compositeDisposable;

    @BindView(R.id.atv_dialog_fragment_seach_seach_box)
    AutoCompleteTextView edtSeachBox;

    @BindView(R.id.rv_dialog_fragment_seach_list_result)
    RecyclerView rvListResult;

    @BindView(R.id.btn_dialog_fragment_seach_seach)
    Button btnSeach;

    @BindView(R.id.pv_loading)
    ProgressView pvLoading;

    @BindView(R.id.tv_cancel)
    TextView tvCancel;

    private List<GroupBillModel> listResult;
    private GroupBillAdapter adapterGroupBill;

    public static SearchDialogFragment newInstance() {
        SearchDialogFragment fragment = new SearchDialogFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_seach, container, false);
        ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setControls();
        getDataSuggest();
        setEvents();
    }

    private void setEvents() {

        tvCancel.setOnClickListener(v -> dismiss());

        btnSeach.setOnClickListener(v -> {

            // Ki???m tra gi?? tr??? t??m ki???m
            if (TextUtils.isEmpty(edtSeachBox.getText().toString())) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getResources().getString(R.string.please_enter_key_to_seach));
                builder.setPositiveButton(getResources().getString(R.string.ok), null);
                builder.show();
                return;
            }

            listResult.clear();
            pvLoading.start();

            String key = edtSeachBox.getText().toString();      // T??? kh??a t??m ki???m

            compositeDisposable.add(getAllBillById(key).subscribeOn(Schedulers.newThread())
                    .filter(capPhats -> capPhats != null)
                    .flatMap(capPhats -> {

                        // T???o k???t qu??? theo m?? phi???u
                        if (capPhats.size() > 0) {
                            GroupBillModel groupBillModel = new GroupBillModel();
                            groupBillModel.setNameGroup(getResources().getString(R.string.code_bill) + ": " + key);
                            groupBillModel.setListBills(capPhats);

                            listResult.add(groupBillModel);
                        }

                        return getAllBillByStaftId(key);

                    }).filter(capPhats -> capPhats != null)
                    .flatMap(capPhats -> {

                        // Kh???i t???o k???t qu??? theo m?? nh??n vi??n
                        if (capPhats.size() > 0) {

                            GroupBillModel groupBillModel = new GroupBillModel();
                            groupBillModel.setNameGroup(getResources().getString(R.string.code_staft) + ": " + key);
                            groupBillModel.setListBills(capPhats);

                            listResult.add(groupBillModel);
                        }

                        return getAllBillByProductId(key);

                    }).filter(capPhats -> capPhats != null)
                    .flatMap(capPhats -> {

                        // Kh???i t???o k???t qu??? theo m?? s???n ph???m
                        if (capPhats.size() > 0) {

                            GroupBillModel groupBillModel = new GroupBillModel();
                            groupBillModel.setNameGroup(getResources().getString(R.string.code_product) +": " + key);
                            groupBillModel.setListBills(capPhats);

                            listResult.add(groupBillModel);
                        }

                        return getAllStaftByName(key);

                    }).filter(nhanViens -> nhanViens != null).flatMap(nhanViens -> {

                        // Kh???i t???o k???t qu??? theo t??n nh??n vi??n
                        if (nhanViens.size() > 0) {

                            GroupBillModel groupBillModel = new GroupBillModel();
                            groupBillModel.setNameGroup(getResources().getString(R.string.staft) + ": " + key);
                            List<CapPhat> list = new ArrayList<>();

                            for (NhanVien nv : nhanViens) {

                                List<CapPhat> listBills = WorkWithDb.getInstance().getAllocationByStaftId(nv.getMaNV());
                                if (listBills != null && listBills.size() > 0) {
                                    list.addAll(listBills);
                                }
                            }

                            if (list.size() > 0) {
                                groupBillModel.setListBills(list);
                                listResult.add(groupBillModel);
                            }

                        }

                        return getAllProductByNameProduct(key);

                    }).filter(vanPhongPhams -> vanPhongPhams != null)
                    .flatMap(vanPhongPhams -> {

                        // Kh???i t???o k???t qu??? theo t??n s???n ph???m
                        if (vanPhongPhams.size() > 0) {

                            GroupBillModel groupBillModel = new GroupBillModel();
                            groupBillModel.setNameGroup(getResources().getString(R.string.product) + ": " + key);

                            List<CapPhat> list = new ArrayList<>();

                            for (VanPhongPham vp : vanPhongPhams) {
                                List<CapPhat> listBills = WorkWithDb.getInstance().getAllocationByProductId(vp.getMaVPP() + "");
                                if (listBills != null && listBills.size() > 0) {
                                    list.addAll(listBills);
                                }
                            }

                            if (list.size() > 0) {
                                groupBillModel.setListBills(list);
                                listResult.add(groupBillModel);
                            }
                        }

                        return Observable.just(true);

                    }).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
                        CustomToast.showToastSuccesstion(getContext(), getResources().getString(R.string.search_succesful), Toast.LENGTH_SHORT);
                    }, throwable -> {
                        pvLoading.stop();
                        CustomToast.showToastError(getContext(), getResources().getString(R.string.occurre_error), Toast.LENGTH_SHORT);
                    }, () -> {
                        pvLoading.stop();
                        if (listResult.size() > 0) {
                            adapterGroupBill.notifyDataSetChanged();
                        }
                    }));


        });

        adapterGroupBill.setListener(new GroupBillAdapter.OnGroupBillAdapterListener() {
            @Override
            public void onItemClick(int positionParent, int positionChild) {    // Xem chi ti???t m???t bill

                CapPhat mItem = listResult.get(positionParent).getListBills().get(positionChild);

                Intent intent = DetailBillActivity.getCallingIntent(getContext());
                intent.putExtra("ID_BILL", mItem.getMaPhieu());
                startActivity(intent);


            }

            @Override
            public void onButtonRemoveClick(int positionParent, int positionChild) {        // X??a s???n ph???m

                CapPhat mItem = listResult.get(positionParent).getListBills().get(positionChild);

                Observable<Boolean> deleteBill = Observable.create(r -> {
                    r.onNext(WorkWithDb.getInstance().delete(mItem));
                    r.onComplete();
                });

                compositeDisposable.add(deleteBill.subscribeOn(Schedulers.newThread())
                        .flatMap(aBoolean -> {  // X??a h??a ????n th??nh c??ng

                            if (aBoolean) {
                                // C???p nh???t l???i s??? l?????ng s???n ph???m
                                VanPhongPham vanPhongPham = WorkWithDb.getInstance().getProductById(mItem.getMaVPP());

                                if (vanPhongPham != null) {
                                    vanPhongPham.setSoLuong(vanPhongPham.getSoLuong() + mItem.getSoLuong());    // C???p nh???t l???i s??? l?????ng

                                    return Observable.just(WorkWithDb.getInstance().update(vanPhongPham));

                                } else {
                                    return  Observable.just(false);
                                }

                            } else {
                                return  Observable.just(false);
                            }

                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {

                            if (aBoolean) { // X??a th??nh c??ng ==> C???p nh???t l???i hi???n th???

                                listResult.get(positionParent).getListBills().remove(positionChild);

                                if (listResult.get(positionParent).getListBills().size() > 0) {
                                    adapterGroupBill.notifyItemChanged(positionParent);
                                } else {
                                    listResult.remove(positionParent);
                                    adapterGroupBill.notifyItemRemoved(positionParent);
                                    adapterGroupBill.notifyItemRangeChanged(positionParent, listResult.size());
                                }

                                CustomToast.showToastSuccesstion(getContext(), getResources().getString(R.string.delete_successful), Toast.LENGTH_SHORT);

                            } else {
                                CustomToast.showToastError(getContext(), getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT);
                            }

                        }, throwable -> {
                            CustomToast.showToastError(getContext(), getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT);
                        }));

            }
        });

    }

    // L???y c??c phi???u theo m?? phi???u
    private Observable<List<CapPhat>> getAllBillById(String id) {
        return Observable.just(WorkWithDb.getInstance().getAllAlocationByIdBill(id));
    }

    // L???y c??c h??a ????n theo m?? nh??n vi??n
    private Observable<List<CapPhat>> getAllBillByStaftId(String key) {
        return Observable.just(WorkWithDb.getInstance().getAllocationByStaftId(key));
    }

    // L???y c??c h??a ????n theo m?? s???n ph???m
    private Observable<List<CapPhat>> getAllBillByProductId(String key) {
        return Observable.just(WorkWithDb.getInstance().getAllocationByIdProduct(key));
    }

    // L???y danh s??ch nh??n vi??n theo t??n
    private Observable<List<NhanVien>> getAllStaftByName(String key) {
        return Observable.just(WorkWithDb.getInstance().getAllStaftByName(key));
    }

    // L???y danh s??ch s???n ph???m theo t??n s???n ph???m
    private Observable<List<VanPhongPham>> getAllProductByNameProduct(String nameProduct) {
        return Observable.just(WorkWithDb.getInstance().getAllProductByName(nameProduct));
    }

    // L???y d??? li???u g???i ??
    private void getDataSuggest() {

        listDataSugestion.clear();

        compositeDisposable.add(getAllBill().subscribeOn(Schedulers.newThread())
                .filter(capPhats -> capPhats != null)
                .flatMap(capPhats -> {

                    // T???o g???i ?? theo m?? phi???u
                    for (CapPhat item : capPhats) {
                        listDataSugestion.add(item.getMaPhieu() + "");
                    }

                    // L???y danh s??ch nh??n vi??n ????? t???o g???i ??
                    return getAllStaft();

                }).filter(nhanViens -> nhanViens != null)
                .flatMap(nhanViens -> {

                    // T???o g???i ?? theo t??n nh??n vi??n
                    for (NhanVien item : nhanViens) {
                        listDataSugestion.add(item.getTenNV());
                    }

                    // L???y danh s???n s???n ph???m ????? t???o g???i ??
                    return getAllProduct();

                }).filter(vanPhongPhams -> vanPhongPhams != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vanPhongPhams -> {

                    // T???o g???i ?? theo t??n s???n ph???m
                    for (VanPhongPham item : vanPhongPhams) {
                        listDataSugestion.add(item.getTenSP());
                    }

                }, throwable -> {

                    CustomToast.showToastError(getContext(), getResources().getString(R.string.occurre_error), Toast.LENGTH_SHORT);

                }, () -> {
                    adapterSugestion.notifyDataSetChanged();
                }));
    }

    private void setControls() {

        compositeDisposable = new CompositeDisposable();

        // Kh???i t???o list k???t qu??? v?? adapter
        listResult = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvListResult.setLayoutManager(linearLayoutManager);
        adapterGroupBill = new GroupBillAdapter(getContext(), listResult);
        rvListResult.setAdapter(adapterGroupBill);

        // Kh???i t???o list c??c gi?? tr??? g???i ?? v?? adpater
        listDataSugestion = new ArrayList<>();
        adapterSugestion = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, listDataSugestion);
        edtSeachBox.setThreshold(1);
        edtSeachBox.setAdapter(adapterSugestion);


    }

    // Observable l???y danh s??ch phi???u
    private Observable<List<CapPhat>> getAllBill() {
        return Observable.just(WorkWithDb.getInstance().getAllAllocation());
    }

    // Observable l???y danh s??ch nh??n vi??n
    private Observable<List<NhanVien>> getAllStaft() {
        return Observable.just(WorkWithDb.getInstance().getAllStaft());
    }

    private Observable<List<VanPhongPham>> getAllProduct() {
        return Observable.just(WorkWithDb.getInstance().getAllProduct());
    }
}
