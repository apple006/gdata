package com.gsralex.gdata.jdbctemplate;

import com.gsralex.gdata.FieldColumn;
import com.gsralex.gdata.FieldValue;
import com.gsralex.gdata.FieldEnum;
import com.gsralex.gdata.SqlCuHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gsralex
 * @date 2018/3/10
 */
public class JdbcTemplateUtils {

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplateUtils(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public <T> boolean insert(T t) {
        if (t == null) {
            return false;
        }
        SqlCuHelper helper = new SqlCuHelper();
        String sql = helper.getInsertSql(t.getClass());
        Object[] objects = helper.getInsertObjects(t);
        return jdbcTemplate.update(sql, objects) != 0 ? true : false;
    }

    public <T> boolean insertWithGeneratedKey(T t) {
        if(t==null){
            return false;
        }
        SqlCuHelper helper = new SqlCuHelper();
        String sql = helper.getInsertSql(t.getClass());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int r = jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps;
        }, keyHolder);

        List<FieldColumn> columnList = helper.getColumns(t.getClass(), FieldEnum.Id);
        Map<String, Object> keyMap = keyHolder.getKeyList().get(0);
        FieldValue fieldValue = new FieldValue(t, t.getClass());
        for (FieldColumn column : columnList) {
            Object key = keyMap.get(column.getAliasName());
            fieldValue.setValue(key.getClass(), column.getName(), key);
        }
        return r != 0 ? true : false;
    }

    public <T> int batchInsert(List<T> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        SqlCuHelper modelUtils = new SqlCuHelper();
        Class<T> type = (Class<T>) list.get(0).getClass();
        String sql = modelUtils.getInsertSql(type);
        List<Object[]> argList = new ArrayList<>();
        for (T item : list) {
            argList.add(modelUtils.getInsertObjects(item));
        }
        return getOkResult(jdbcTemplate.batchUpdate(sql, argList));
    }

    private static int getOkResult(int[] r) {
        int cnt = 0;
        for (int item : r) {
            if (item != Statement.EXECUTE_FAILED) {
                cnt++;
            }
        }
        return cnt;
    }

    public <T> boolean update(T t) {
        if (t == null) {
            return false;
        }
        SqlCuHelper modelUtils = new SqlCuHelper();
        String sql = modelUtils.getUpdateSql(t.getClass());
        Object[] objects = modelUtils.getUpdateObjects(t);
        return jdbcTemplate.update(sql, objects) != 0 ? true : false;
    }

    public <T> int batchUpdate(List<T> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        SqlCuHelper modelUtils = new SqlCuHelper();
        Class<T> type = (Class<T>) list.get(0).getClass();
        String sql = modelUtils.getUpdateSql(type);
        List<Object[]> argList = new ArrayList<>();
        for (T item : list) {
            argList.add(modelUtils.getUpdateObjects(item));
        }
        return getOkResult(jdbcTemplate.batchUpdate(sql, argList));
    }


    public <T> T get(String sql, Object[] args, Class<T> type) {
        List<T> list = jdbcTemplate.query(sql, args, new BeanRowMapper<>(type));
        if (list != null && list.size() != 0) {
            return list.get(0);
        }
        return null;
    }

    public <T> List<T> getList(String sql, Object[] args, Class<T> type) {
        return jdbcTemplate.query(sql, args, new BeanRowMapper<>(type));
    }


    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}