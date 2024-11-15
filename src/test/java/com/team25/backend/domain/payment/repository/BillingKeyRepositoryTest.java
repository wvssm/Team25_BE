package com.team25.backend.domain.payment.repository;

import com.team25.backend.domain.payment.entity.BillingKey;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import jakarta.transaction.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BillingKeyRepositoryTest {

    @Autowired
    private BillingKeyRepository billingKeyRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUsername", "test-uuid", "ROLE_USER");
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        billingKeyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("findByUserUuid 메서드가 올바르게 BillingKey를 반환하는지 테스트")
    @Transactional
    void testFindByUserUuid() {
        BillingKey billingKey = new BillingKey();
        billingKey.setUser(user);
        billingKey.setBid("encrypted-bid");
        billingKey.setCardCode("123");
        billingKey.setCardName("Test Card");
        billingKey.setOrderId("order-123");
        billingKey.setCardAlias("My Card");
        billingKeyRepository.save(billingKey);

        Optional<BillingKey> retrievedBillingKey = billingKeyRepository.findByUserUuid(user.getUuid());

        assertTrue(retrievedBillingKey.isPresent());
        assertEquals("encrypted-bid", retrievedBillingKey.get().getBid());
        assertEquals("test-uuid", retrievedBillingKey.get().getUser().getUuid());
    }

    @Test
    @DisplayName("존재하지 않는 userUuid로 조회 시 빈 결과를 반환하는지 테스트")
    void testFindByUserUuid_NotFound() {
        Optional<BillingKey> retrievedBillingKey = billingKeyRepository.findByUserUuid("non-existent-uuid");

        assertFalse(retrievedBillingKey.isPresent());
    }
}
