//package com.example.productservice.service;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import com.example.productservice.model.Product;
//import com.example.productservice.repository.ProductRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class ProductServiceTest {
//
//    @Mock
//    private ProductRepository productRepository;
//
//
//    @InjectMocks
//    private ProductService productService;
//
//    Product product = new Product();
//
//
//    @BeforeEach
//    public void setUp() {
//        product.setId(1L);
//        product.setName("test");
//        product.setPrice(BigDecimal.TEN);
//        product.setQuantity(1);
//    }
//
//
//    @Test
//    public void getAllProducts_success() throws Exception {
//        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
//
//        List<Product> products = productService.getAllProducts();
//
//        assertNotNull(products);
//        assertNotNull(products);
//        assertEquals(1, products.size());
//        assertEquals("test", products.get(0).getName());
//
//        verify(productRepository, times(1)).findAll();
//    }
//
//
//    @Test
//    void testGetProductById() {
//        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
//
//        Optional<Product> foundProduct = productService.getProductById(1L);
//
//        assertTrue(foundProduct.isPresent());
//        assertEquals("test", foundProduct.get().getName());
//
//        verify(productRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void testSaveProduct() {
//        when(productRepository.save(product)).thenReturn(product);
//
//        Product savedProduct = productService.saveProduct(product);
//
//        assertNotNull(savedProduct);
//        assertEquals("test", savedProduct.getName());
//
//        verify(productRepository, times(1)).save(product);
//    }
//
//    @Test
//    void testDeleteProduct() {
//        doNothing().when(productRepository).deleteById(anyLong());
//        productService.deleteProduct(1L);
//
//        verify(productRepository, times(1)).deleteById(1L);
//    }
//}
