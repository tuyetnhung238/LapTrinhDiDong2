package com.example.quanlyvpp.presentation.bottom_sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlyvpp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectPhotoBottomSheet extends BottomSheetDialogFragment {

    public static String TAG = SelectPhotoBottomSheet.class.getSimpleName();

    @BindView(R.id.tv_bottom_sheet_select_close)
    TextView tvClose;

    @BindView(R.id.lnl_area_select_from_library)
    LinearLayout lnlSelectFromLibrary;

    @BindView(R.id.lnl_area_select_from_camera)
    LinearLayout lnlSelectFromCamera;

    public interface SelectPhotoDialogListener {
        void onSelectFromLibrary();

        void onSelectFromCamera();

    }

    private SelectPhotoDialogListener mListener;

    public void setListener(SelectPhotoDialogListener listener) {
        this.mListener = listener;
    }

    public static SelectPhotoBottomSheet newInstance() {
        Bundle args = new Bundle();
        SelectPhotoBottomSheet fragment = new SelectPhotoBottomSheet();
        fragment.setArguments(args);
        return fragment;

    }

    public SelectPhotoBottomSheet() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_select_photo, container, false);
        ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setEvents();

    }

    private void setEvents() {

        tvClose.setOnClickListener(v -> dismiss());

        lnlSelectFromCamera.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSelectFromCamera();
                dismiss();
            }
        });

        lnlSelectFromLibrary.setOnClickListener(v -> {
            mListener.onSelectFromLibrary();
            dismiss();
        });

    }


}
