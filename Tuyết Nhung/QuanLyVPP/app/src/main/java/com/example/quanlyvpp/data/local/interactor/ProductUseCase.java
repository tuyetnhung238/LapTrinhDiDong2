package com.example.quanlyvpp.data.local.interactor;

import com.example.quanlyvpp.data.local.model.VanPhongPham;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public class ProductUseCase {


    public static int create(final Dao<VanPhongPham, Long> dao, VanPhongPham vanPhongPham) throws SQLException {
        return dao.create(vanPhongPham);
    }

    public static int update(final Dao<VanPhongPham, Long> dao, VanPhongPham vanPhongPham)throws SQLException{
        return dao.update(vanPhongPham);
    }

    public static int delete(final Dao<VanPhongPham, Long> dao, VanPhongPham vanPhongPham)throws SQLException{
        return dao.delete(vanPhongPham);
    }

    public static VanPhongPham getById(final Dao<VanPhongPham, Long> dao, long id)throws SQLException{
        return dao.queryForId(id);
    }

    public static List<VanPhongPham> getAll(final Dao<VanPhongPham, Long> dao) throws SQLException{
        return dao.queryForAll();
    }

    public static List<VanPhongPham> getAllProductByProductName(final Dao<VanPhongPham, Long> dao, String productName) throws SQLException{
        return dao.queryBuilder().where().like("TenSP", "%" + productName + "%").query();
    }

    public static List<VanPhongPham> getTopProductView(final Dao<VanPhongPham, Long> dao) throws SQLException{
        return dao.queryBuilder().orderByRaw("DaDung DESC").limit((long) 10).query();
    }

}

