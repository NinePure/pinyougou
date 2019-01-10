package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao catDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //1. 查询所有分类数据
        List<ItemCat> itemAllList = catDao.selectByExample(null);
        //2. 将所有分类数据以分类名称为key, 对应的模板id为value, 存入redis中
        for (ItemCat itemCat : itemAllList) {
            redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).put(itemCat.getName(), itemCat.getTypeId());
        }

        //3. 到数据库中查询数据返回到页面展示
        ItemCatQuery query = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = query.createCriteria();
        //根据父级id查询
        criteria.andParentIdEqualTo(parentId);
        List<ItemCat> itemCats = catDao.selectByExample(query);
        return itemCats;
    }

    @Override
    public ItemCat findOne(Long id) {
        return catDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ItemCat> findAll() {
        return catDao.selectByExample(null);
    }





    //    添加分类申请
    @Override
    public Boolean save(String itemcat, Long id) {
//               更据名字条件查询是否分类存在
        ItemCatQuery query = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = query.createCriteria();
        criteria.andNameEqualTo(itemcat);
        List<ItemCat> itemCatList = catDao.selectByExample(query);

//        如果name和id都为空则是一级分类,创建分类
        if (itemCatList==null && id==null){
            //  如果返回的集合为空就创建
            ItemCat itemCat = new ItemCat();
            itemCat.setParentId(0L);
            itemCat.setName(itemcat);
            itemCat.setStatus("0");
            itemCat.setTypeId(35L);
            //        保存数据到数据库
            catDao.insertSelective(itemCat);
            return true;
        }else if (itemCatList!=null && id==null){
//            如果返回的集合不是空而id是空说明一级分类已经存在无法创建
            return false;
        }else if (itemCatList==null && id!=null){
//            如果返回的集合是空,而返回的id不是空说明是子级数据.
            ItemCat itemCat = new ItemCat();
            itemCat.setParentId(id);
            itemCat.setName(itemcat);
            itemCat.setStatus("0");
            itemCat.setTypeId(35L);
            //        保存数据到数据库
            catDao.insertSelective(itemCat);
            return true;
        }else{
            return false;
        }
    }



}
