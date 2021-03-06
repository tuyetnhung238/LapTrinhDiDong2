package com.example.quanlyvpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlyvpp.data.local.WorkWithDb;
import com.example.quanlyvpp.data.local.model.VanPhongPham;
import com.example.quanlyvpp.presentation.activity.NewProductActivity;
import com.example.quanlyvpp.presentation.adapter.GroupProductAdapter;
import com.example.quanlyvpp.presentation.baseview.BaseFragment;
import com.example.quanlyvpp.presentation.dialog_fragment.ImportProductDialog;
import com.example.quanlyvpp.presentation.dialog_fragment.ShowDetailProductDialog;
import com.example.quanlyvpp.presentation.model.GroupProductModel;
import com.example.quanlyvpp.until.CustomToast;
import com.example.quanlyvpp.until.GetDataToCommunicate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProductManagerFragment extends BaseFragment {

    @BindView(R.id.rv_fragment_stationary_manager_list_product)
    RecyclerView rvListProducts;

    @BindView(R.id.fab)
    FloatingActionButton fbAdd;

    @BindView(R.id.lnl_fragment_stationary_manager_notify_emplty)
    LinearLayout lnlNotifyEmplty;

    @BindView(R.id.tv_fragment_stationary_manager_total_product)
    TextView tvTotalProduct;

    @BindView(R.id.tv_fragment_stationary_manager_total_price)
    TextView tvTotalPrice;

    private GroupProductAdapter groupProductAdapter;
    private List<GroupProductModel> groupProductModels;     // Danh s??ch nh??m s???n ph???m theo Aplha
    private CompositeDisposable compositeDisposable;

    private int totalProduct = 0;      //  T???ng s??? s???n ph???m
    private double totalPrice = 0;      // T???ng gi?? tr???

    public static ProductManagerFragment newInstance() {
        ProductManagerFragment fragment = new ProductManagerFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stationary_manager, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        setControls();
        getDatas();
        setEvents();

    }

    private void setEvents() {


        // Th??m s???n ph???m
        fbAdd.setOnClickListener(v -> {
            Intent intent = NewProductActivity.getCallingIntent(getContext());
            startActivityForResult(intent, NewProductActivity.KEY_ADD_PRODUCT);

            // Animation chuy???n c???nh
            getActivity().overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        });

        groupProductAdapter.setListener(new GroupProductAdapter.GroupProductApapterListener() {
            @Override
            public void onProductClick(VanPhongPham item) {     // Ch???nh s???a s???n ph???m

                Intent intent = NewProductActivity.getCallingIntent(getContext());
                intent.putExtra("PRODUCT_ID", item.getMaVPP());
                startActivityForResult(intent, NewProductActivity.KEY_ADD_PRODUCT);
                // Animation chuy???n c???nh
                getActivity().overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

            }

            @Override
            public void onDeleteProduct(int positionGroup, VanPhongPham itemDelete) {       // X??a s???n ph???m

                Observable<Boolean> obRemoveProduct = Observable.create(r -> {
                    try {
                        r.onNext(WorkWithDb.getInstance().delete(itemDelete));
                    } catch (Exception e) {
                        r.onError(e);
                    }
                });

                compositeDisposable.add(obRemoveProduct.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {     // X??a th??nh c??ng
                                CustomToast.showToastSuccesstion(getContext(), getResources().getString(R.string.delete_successful), Toast.LENGTH_SHORT);

                                groupProductModels.get(positionGroup).getVanPhongPhamList().remove(itemDelete);

                                if (groupProductModels.get(positionGroup).getVanPhongPhamList().size() > 0) {   // Ch??? x??a s???n ph???m
                                    groupProductAdapter.notifyItemChanged(positionGroup);

                                } else {        // X??a lu??n c??? nh??m v?? h???t s???n ph???m
                                    groupProductModels.remove(positionGroup);
                                    groupProductAdapter.notifyItemRemoved(positionGroup);
                                    groupProductAdapter.notifyItemRangeChanged(positionGroup, groupProductModels.size());
                                }

                            } else {
                                CustomToast.showToastError(getContext(),  getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT);
                            }

                        }, throwable -> {
                            CustomToast.showToastError(getContext(),  getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT);
                        }));
            }

            @Override
            public void onImportProduct(int positionGroup, int positionChild) {     // Nh???p h??ng

                ImportProductDialog importProductDialog = ImportProductDialog.newInstance();

                importProductDialog.setListener(amount -> {

                    VanPhongPham vanPhongPham = groupProductModels.get(positionGroup).getVanPhongPhamList().get(positionChild);
                    vanPhongPham.setSoLuong(vanPhongPham.getSoLuong() + amount);
                    WorkWithDb.getInstance().update(vanPhongPham);
                    groupProductAdapter.notifyItemChanged(positionGroup);
                    CustomToast.showToastSuccesstion(getContext(), getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT);

                });

                importProductDialog.show(getChildFragmentManager(), "");
            }

            @Override
            public void onItemClick(VanPhongPham item) {        // Hi???n th??? chi ti???t s???n ph???m

                ShowDetailProductDialog showDetailProductDialog = ShowDetailProductDialog.newInstance();
                showDetailProductDialog.setProduct(item);   // Truy???n d??? li???u s???n ph???m
                showDetailProductDialog.show(getChildFragmentManager(), "");
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    // L???y danh s??ch s???n ph???m
    private void getDatas() {

        totalProduct = 0;
        totalPrice = 0;

        compositeDisposable.add(
                getAllProduct().subscribeOn(Schedulers.newThread())
                        .flatMap(list -> {

                            Map<String, List<VanPhongPham>> listGroup = new HashMap<>();

                            // Th???c hi???n gom nh??m th??m Alpha
                            for (VanPhongPham item : list) {

                                String amlpha = item.getTenSP().substring(0, 1);        // L???y k?? t??? ?????u

                                if (listGroup.get(amlpha) == null) {        // K?? t??? ch??a kh???i t???o d??? li???u

                                    List<VanPhongPham> listItem = new ArrayList<>();
                                    listItem.add(item);
                                    listGroup.put(amlpha, listItem);

                                    // T??nh t???n s??? l?????ng v?? ????n gi??
                                    totalProduct += item.getSoLuong();
                                    totalPrice += item.getDonGia() * item.getSoLuong();


                                } else {        // K?? t??? ???? c?? s???n ph???m r???i

                                    List<VanPhongPham> listItem = listGroup.get(amlpha);
                                    listItem.add(item);
                                    listGroup.put(amlpha, listItem);

                                    // T??nh t???n s??? l?????ng v?? ????n gi??
                                    totalProduct += item.getSoLuong();
                                    totalPrice += item.getDonGia() * item.getSoLuong();
                                }
                            }

                            // T???o ?????i t?????ng gom nh??m c??c s???n ph???m
                            List<GroupProductModel> listGroupProduct = new ArrayList<>();

                            for (Map.Entry<String, List<VanPhongPham>> entry : listGroup.entrySet()) {

                                GroupProductModel groupProductModel = new GroupProductModel();
                                groupProductModel.setTextAlpha(entry.getKey());         // Alpha
                                groupProductModel.setVanPhongPhamList(entry.getValue());    // Danh s??ch s???n ph???m
                                listGroupProduct.add(groupProductModel);
                            }

                            return Observable.just(listGroupProduct);

                        }).observeOn(AndroidSchedulers.mainThread()).subscribe(list -> {

                    if (list.size() > 0) {  // ???? c?? s???n ph???m

                        tvTotalProduct.setText(totalProduct + "");
                        tvTotalPrice.setText(GetDataToCommunicate.changeToPrice(totalPrice) + "");

                        lnlNotifyEmplty.setVisibility(View.GONE);
                        rvListProducts.setVisibility(View.VISIBLE);

                        groupProductModels.clear();
                        groupProductModels.addAll(list);
                        groupProductAdapter.notifyDataSetChanged();

                    } else {        // Th??ng b??o ch??a c?? s???n ph???m

                        lnlNotifyEmplty.setVisibility(View.VISIBLE);
                        rvListProducts.setVisibility(View.GONE);

                    }

                }, throwable -> {
                    CustomToast.showToastError(getContext(), getResources().getString(R.string.occurre_error), Toast.LENGTH_SHORT);
                })
        );

    }

    private void setControls() {

        compositeDisposable = new CompositeDisposable();

        // Kh???i t???o danh s??ch v?? adapter
        groupProductModels = new ArrayList<>();
        groupProductAdapter = new GroupProductAdapter(getContext(), groupProductModels);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvListProducts.setLayoutManager(linearLayoutManager);
        rvListProducts.setAdapter(groupProductAdapter);
    }

    // Ob l???y danh s??ch s???n ph???m
    private Observable<List<VanPhongPham>> getAllProduct() {

        return Observable.create(r -> {
            try {

                List<VanPhongPham> list = WorkWithDb.getInstance().getAllProduct();
                r.onNext(list);
                r.onComplete();

            } catch (Exception e) {
                r.onError(e);
            }

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        // Nh???n k???t qu??? th??m s???n ph???m
        if (requestCode == NewProductActivity.KEY_ADD_PRODUCT && requestCode == NewProductActivity.KEY_ADD_PRODUCT) {
            getDatas();
        }

        // Nh???n k???t qu??? ch???nh s???a s???n ph???m
        if (requestCode == NewProductActivity.KEY_EDIT_PRODUCT && resultCode == NewProductActivity.KEY_EDIT_PRODUCT) {
            getDatas();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
