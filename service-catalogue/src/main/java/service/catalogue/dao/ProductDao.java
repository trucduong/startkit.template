package service.catalogue.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import core.common.convert.ConverterUtils;
import core.dao.utils.BaseDao;
import core.dao.utils.QueryBuilder;
import service.catalogue.entities.Product;
import service.catalogue.entities.ProductDetail;
import service.catalogue.importexport.ProductPricesSheet;
import service.catalogue.shared.dto.ProductDto;
import service.catalogue.shared.utils.ProductStatus;
import service.catalogue.translation.ProductGroupTranslation;

@Repository
public class ProductDao extends BaseDao<Product> {
	private static final long serialVersionUID = 1L;

	@Autowired
	private ProductGroupTranslation groupTranslation;

	@Autowired
	private ProductDetailDao detailDao;

	public List<ProductDto> getAllWithDetail(ProductStatus status) {
		Map<Long, ProductDto> productMap = new HashMap<>();
		List<ProductDto> dtos = new ArrayList<ProductDto>();
		List<Product> entities = this.getAllDataByColumn(Product.STATUS, ProductStatus.ACTIVE, Product.NAME);
		for (Product entity : entities) {
			ProductDto dto = new ProductDto();
			entity.unBind(dto);

			productMap.put(dto.getId(), dto);
			dtos.add(dto);
		}

		// get details
		if (!productMap.isEmpty()) {
			List<ProductDetail> details = detailDao.getAllDataByColumns(ProductDetail.ID, productMap.keySet().toArray());
			for (ProductDetail productDetail : details) {
				ProductDto dto = productMap.get(productDetail.getId());
				productDetail.unBind(dto);
			}
		}

		return dtos;
	}

	public List<ProductPricesSheet> getProductPriceToExport(ProductStatus status) {
		Map<Long, ProductPricesSheet> productMap = new HashMap<>();
		List<ProductPricesSheet> products = new ArrayList<ProductPricesSheet>();
		QueryBuilder builder = new QueryBuilder();
		builder.append("SELECT p.id, p.name, p.group FROM Product as p WHERE 1=1");
		if (status != null) {
			builder.append(" AND p.status = :status", "status", ProductStatus.ACTIVE);
		}

		Query query = builder.build(getEm());
		List<Object[]> resultList = query.getResultList();
		for (Object[] objects : resultList) {
			ProductPricesSheet dto = new ProductPricesSheet();
			int i = 0;
			dto.setId(ConverterUtils.toLong(objects[i++]));
			dto.setName(ConverterUtils.toString(objects[i++]));
			dto.setGroup(groupTranslation.translate(ConverterUtils.toString(objects[i++])));

			products.add(dto);
			productMap.put(dto.getId(), dto);
		}

		// get details
		if (!productMap.isEmpty()) {
			List<ProductDetail> details = detailDao.getAllDataByColumns(ProductDetail.ID, productMap.keySet().toArray());
			for (ProductDetail productDetail : details) {
				ProductPricesSheet dto = productMap.get(productDetail.getId());
				dto.setDiscount(productDetail.getDiscount());
				dto.setRemaining(productDetail.getRemaining());
				dto.setInputPrice(productDetail.getInputPrice());
				dto.setWholesalePrice(productDetail.getWholesalePrice());
				dto.setRetailPrice(productDetail.getRetailPrice());
			}
		}

		return products;
	}
	
	public List<ProductDto> getProductRef(ProductStatus status) {
		List<ProductDto> products = new ArrayList<ProductDto>();
		QueryBuilder builder = new QueryBuilder();
		builder.append("SELECT p.id, p.name FROM Product as p WHERE 1=1");
		if (status != null) {
			builder.append(" AND p.status = :status", "status", ProductStatus.ACTIVE);
		}

		Query query = builder.build(getEm());
		List<Object[]> resultList = query.getResultList();
		for (Object[] objects : resultList) {
			ProductDto dto = new ProductDto();
			int i = 0;
			dto.setId(ConverterUtils.toLong(objects[i++]));
			dto.setName(ConverterUtils.toString(objects[i++]));

			products.add(dto);
		}
		
		return products;
	}
}
