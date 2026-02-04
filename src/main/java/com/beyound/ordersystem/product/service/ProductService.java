package com.beyound.ordersystem.product.service;

import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.repository.MemberRepository;
import com.beyound.ordersystem.product.dto.ProductCreateDto;
import com.beyound.ordersystem.product.dto.ProductListDto;
import com.beyound.ordersystem.product.dto.ProductSearchDto;
import com.beyound.ordersystem.product.dto.ProductUpdateDto;
import com.beyound.ordersystem.product.entity.Product;
import com.beyound.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${aws.s3.bucket1}")
    private String buctket;

    @Autowired
    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }

    public Product create(ProductCreateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("pro_ser_create"));
        Product product = productRepository.save(dto.toEntity(member));
        System.out.println(dto.getProductImage());
        if (dto.getProductImage() != null) {
            String fileName = "product-" + product.getId() + "-" + dto.getProductImage().getOriginalFilename();
            System.out.println(fileName);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(buctket)
                    .key(fileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String imgurl = s3Client.utilities().getUrl(a -> a.bucket(buctket).key(fileName)).toExternalForm();
            product.updateProfileImageUrl(imgurl);
        }
//        동시성 문제 해결을 위해 상품 등록시 redis에 제고세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));
        return product;
    }

    @Transactional(readOnly = true)
    public ProductListDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("pro_ser_findByid"));
        return ProductListDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProductListDto> findAll(Pageable pageable, ProductSearchDto dto) {

        Specification<Product> specification = new Specification<Product>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if (dto.getName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + dto.getName() + "%"));
                }
                if (dto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), dto.getCategory()));
                }
                Predicate[] predicatesArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateList.size(); i++) {
                    predicatesArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicatesArr);
                return predicate;
            }
        };

        Page<Product> postList = productRepository.findAll(specification, pageable);
        return postList.map(ProductListDto::fromEntity);
    }


    public void update(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 ID prod_ser_update"));
        product.updateProduct(dto);

        if (dto.getProductImage() != null) {
//            이미지를 수정하거나 추가하는 경우 : 삭제 후 추가 (이미지 수정할 시 그냥 부분수정말고 전부 삭제 후 추가 로직으로 구현)
//            기존 이미지를 파일명으로 삭제

            if (product.getImagePath() != null) {
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(buctket).key(fileName));
            }
//                신규 이미지를 등록
            String newfileName = "product-" + product.getId() + "-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(buctket)
                    .key(newfileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(buctket).key(newfileName)).toExternalForm();
            product.updateProfileImageUrl(newImgUrl);
        } else {
//            이미지를 삭제하고자 하는 경우
            if (product.getImagePath() != null) {
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(a -> a.bucket(buctket).key(fileName));
            }
        }
    }
}
