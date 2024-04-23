package com.tobeto.repository.warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tobeto.entities.warehouse.ShelfProduct;

import jakarta.transaction.Transactional;

public interface ShelfProductRepository extends JpaRepository<ShelfProduct, UUID> {
	List<ShelfProduct> findByShelfId(UUID id);

	Optional<ShelfProduct> findFirstByProductId(UUID id);

	@Query("SELECT sp.product.id, SUM(sp.productCount) AS total_product_count, p.name FROM ShelfProduct sp INNER JOIN sp.product p GROUP BY sp.product.id, p.name ORDER BY sp.product.id")
	List<Object[]> getAllProductsFromShelves();

	// gelen raf id si içinde o üründen kaç tane var ona bakıyor
	@Query("SELECT sp.productCount FROM ShelfProduct sp WHERE sp.shelf.id = :shelfId AND sp.product.id = :productId")
	int findProductCountByShelfIdAndProductId(UUID shelfId, UUID productId);

	// gönderim yapmak istediğimiz ürünün id si ile içinde istenilen ürün olan
	// ve
	// dolu olmayan
	// raf var mı diye bakıyor
	// burada shelf id i dönüyor bunu shelf e çevirmem lazım !!!

	// @Query("SELECT sp.shelf.id FROM ShelfProduct sp JOIN Shelf shelf ON
	// sp.shelf.id = shelf.id WHERE sp.product.id = :productId GROUP BY
	// sp.shelf.id,
	// shelf.capacity HAVING SUM(sp.productCount) < shelf.capacity")

//	@Query("SELECT sp.id, sp.productCount, sp.product.id, sp.shelf.id FROM ShelfProduct sp JOIN sp.shelf shelf WHERE sp.product.id = :productId GROUP BY sp.id, shelf.id HAVING SUM(sp.productCount) < MAX(shelf.capacity)")
//	Optional<ShelfProduct> findByProductIdNotFull(int productId);

	@Query("SELECT sp FROM ShelfProduct sp JOIN sp.shelf shelf WHERE sp.product.id = :productId GROUP BY sp.id, shelf.id HAVING SUM(sp.productCount) < MAX(shelf.capacity) ORDER BY SUM(sp.productCount) ASC LIMIT 1")
	Optional<ShelfProduct> findByProductIdNotFull(UUID productId);

	@Query("SELECT sp FROM ShelfProduct sp WHERE sp.product.id = :productId ORDER BY sp.productCount ASC")
	Optional<ShelfProduct> findByProductIdOrderByProductCountAsc(UUID productId);

	// gelen id'li rafta id'si verilen ürünü raftan çıkar
	@Transactional
	@Modifying
	@Query("DELETE FROM ShelfProduct sp WHERE sp.shelf.id = :shelfId AND sp.product.id = :productId")
	void deleteProductFromShelf(UUID shelfId, UUID productId);

	// istenilen idli ürünün olduğu rafları buluyor ve girilen miktardan fazla
	// olanları listeliyor.
	// birden fazla raf dönebilir geri dönüş yoluna bak
	// burada ürün sayısı az olandan çok olana göre sırala

	@Query("SELECT sp FROM ShelfProduct sp WHERE sp.product.id = :productId AND sp.productCount > :count ORDER BY sp.productCount DESC")
	List<ShelfProduct> findByProductIdAndProductCountGreaterThan(UUID productId, int count);

}