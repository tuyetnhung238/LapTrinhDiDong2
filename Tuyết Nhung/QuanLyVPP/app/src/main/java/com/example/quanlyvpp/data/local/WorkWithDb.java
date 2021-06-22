package com.example.quanlyvpp.data.local;

import android.content.Context;

import com.example.quanlyvpp.data.local.interactor.ProductUseCase;
import com.example.quanlyvpp.data.local.model.VanPhongPham;
import com.example.quanlyvpp.presentation.baseview.MyApp;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WorkWithDb {

    private static DatabaseHelper databaseHelper = null;
    private static final String TAG = WorkWithDb.class.getSimpleName();
    private static WorkWithDb workWithDb = null;
    private Context mContext;

    public static WorkWithDb getInstance(){
        if(workWithDb == null){
            workWithDb = new WorkWithDb();
        }
        return workWithDb;
    }

    public WorkWithDb(){
        mContext = MyApp.getAppContext();
        databaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
    }



    // Van phòng phẩm table ---------------------------------------


    // Thêm sản phẩm

    public boolean insert(VanPhongPham vanPhongPham) {
        try {
            return  ProductUseCase.create(databaseHelper.getProductDao(), vanPhongPham) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    // Cập nhật sản phẩm
    public boolean update(VanPhongPham vanPhongPham) {
        try {
            ProductUseCase.update(databaseHelper.getProductDao(), vanPhongPham);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa sản phẩm
    public boolean delete(VanPhongPham vanPhongPham) {
        try {
            ProductUseCase.delete(databaseHelper.getProductDao(), vanPhongPham);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy san phẩm theo id
    public VanPhongPham getProductById(long id) {
        try {
            return ProductUseCase.getById(databaseHelper.getProductDao(), id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy danh sách sản phẩm
    public List<VanPhongPham> getAllProduct() {
        try {
            List<VanPhongPham> list = ProductUseCase.getAll(databaseHelper.getProductDao());
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Lấy danh sách sản phẩm theo tên sản phẩm
    public List<VanPhongPham> getAllProductByName(String nameProduct) {
        try {
            List<VanPhongPham> list = ProductUseCase.getAllProductByProductName(databaseHelper.getProductDao(), nameProduct);
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<VanPhongPham> getTopProducts() {
        try {
            List<VanPhongPham> list = ProductUseCase.getTopProductView(databaseHelper.getProductDao());
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }











}

