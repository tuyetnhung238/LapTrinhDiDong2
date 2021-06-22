package com.example.quanlyvpp.presentation.dialog_fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlyvpp.R;
import com.example.quanlyvpp.data.local.model.VanPhongPham;
import com.example.quanlyvpp.presentation.baseview.FullScreenDialog;
import com.example.quanlyvpp.until.CustomToast;
import com.example.quanlyvpp.until.GetDataToCommunicate;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowDetailProductDialog extends FullScreenDialog {

    @BindView(R.id.tv_dialog_show_detail_product_name_product)
    TextView tvNameProduct;

    @BindView(R.id.ibt_dialog_show_detail_product_close)
    ImageButton ibtRemove;

    @BindView(R.id.iv_dialog_show_detail_product_image)
    ImageView ivPicture;

    @BindView(R.id.tv_dialog_show_detail_product_amount)
    TextView tvAmount;

    @BindView(R.id.tv_dialog_show_detail_product_price)
    TextView tvPrice;

    @BindView(R.id.tv_dialog_show_detail_product_unit)
    TextView tvUnit;

    @BindView(R.id.tv_dialog_show_detail_product_note)
    TextView tvNote;

    private VanPhongPham product;

    public static ShowDetailProductDialog newInstance() {
        ShowDetailProductDialog fragment = new ShowDetailProductDialog();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_show_detail_product, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (product != null) {

            tvNameProduct.setText(product.getTenSP());
            tvAmount.setText(product.getSoLuong() + "");

            if (!TextUtils.isEmpty(product.getAnh())) {
                Picasso.get().load(new File(product.getAnh())).error(R.mipmap.app_icon).fit().centerInside().into(ivPicture);
            } else {
                ivPicture.setImageResource(R.drawable.ic_part_color_24);
            }

            tvPrice.setText(getResources().getString(R.string.price) + ": "+ GetDataToCommunicate.changeToPrice(product.getDonGia()));

            tvUnit.setText(!TextUtils.isEmpty(product.getDonVi()) ? getResources().getString(R.string.unit) + ": " + product.getDonVi()  : "");

            if (!TextUtils.isEmpty(product.getGhiChu())) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(getResources().getString(R.string.note) + ": " + product.getGhiChu());
            } else {
                tvNote.setVisibility(View.GONE);
            }

        } else {
            CustomToast.showToastError(getContext(), getResources().getString(R.string.can_not_get_info_proudct), Toast.LENGTH_SHORT);
            dismiss();
        }

        ibtRemove.setOnClickListener(v -> dismiss());
    }


    public void setProduct(VanPhongPham product) {
        this.product = product;
    }
}

