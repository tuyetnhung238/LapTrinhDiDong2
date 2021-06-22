package com.example.quanlyvpp.data.local;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlyvpp.R;
import com.example.quanlyvpp.data.local.model.VanPhongPham;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    // ---------------------------------------------------------------


    private Dao<VanPhongPham, Long> productDao;



    // --------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

        try {


            TableUtils.createTable(connectionSource, VanPhongPham.class);


        } catch (SQLException | java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public <T>void cleanTable(Class<T> modelClass) throws java.sql.SQLException {
        try {
            TableUtils.clearTable(connectionSource, modelClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TableUtils.dropTable(connectionSource, modelClass, true);
        TableUtils.createTable(connectionSource, modelClass);

    }




    public Dao<VanPhongPham, Long> getProductDao() throws java.sql.SQLException {
        if (productDao == null) {
            productDao = getDao(VanPhongPham.class);
        }
        return productDao;
    }


}

