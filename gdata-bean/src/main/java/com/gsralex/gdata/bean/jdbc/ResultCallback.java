package com.gsralex.gdata.bean.jdbc;

import java.sql.ResultSet;

/**
 * @author gsralex
 * @version 2018/4/1
 */
public interface ResultCallback {

    void mapper(ResultSet rs);
}
