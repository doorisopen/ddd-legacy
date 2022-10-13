package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuGroupRepository;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.MenuRepository;
import kitchenpos.domain.Product;
import kitchenpos.domain.ProductRepository;
import kitchenpos.domain.ProfanityClient;
import kitchenpos.helper.InMemoryMenuGroupRepository;
import kitchenpos.helper.InMemoryMenuRepository;
import kitchenpos.helper.InMemoryProductRepository;
import kitchenpos.helper.InMemoryProfanityClient;
import kitchenpos.helper.MenuFixture;
import kitchenpos.helper.MenuGroupFixture;
import kitchenpos.helper.MenuProductFixture;
import kitchenpos.helper.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MenuServiceTest {

    private MenuRepository menuRepository;
    private MenuGroupRepository menuGroupRepository;
    private ProductRepository productRepository;
    private ProfanityClient profanityClient;

    private MenuService testTarget;

    @BeforeEach
    void setUp() {
        menuRepository = new InMemoryMenuRepository();
        menuGroupRepository = new InMemoryMenuGroupRepository();
        productRepository = new InMemoryProductRepository();
        profanityClient = new InMemoryProfanityClient();
        testTarget = new MenuService(
            menuRepository,
            menuGroupRepository,
            productRepository,
            profanityClient
        );
    }

    @DisplayName("메뉴 등록 테스트")
    @Nested
    class CreateTest {

        @DisplayName("메뉴를 등록 할 수 있다.")
        @Test
        void test01() {
            // given
            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Product product = productRepository.save(ProductFixture.FRIED_CHICKEN.get());

            Menu request = MenuFixture.createMenuRequest(menuGroup.getId(), product.getId());

            // when
            Menu actual = testTarget.create(request);

            // then
            assertAll(
                () -> assertThat(actual.getId()).isNotNull(),
                () -> assertThat(actual.getName()).isEqualTo(request.getName()),
                () -> assertThat(actual.getPrice()).isEqualTo(request.getPrice()),
                () -> assertThat(actual.getMenuGroup().getId()).isEqualTo(request.getMenuGroupId()),
                () -> assertThat(actual.isDisplayed()).isEqualTo(request.isDisplayed()),
                () -> assertThat(actual.getMenuProducts())
                    .singleElement()
                    .matches(menuProduct -> menuProduct.getQuantity() == 1)
                    .extracting(MenuProduct::getProduct)
                    .extracting(Product::getId)
                    .matches(productId -> productId.equals(product.getId()))
            );
        }

        @DisplayName("메뉴 가격은 0원 미만일 수 없다.")
        @Test
        void test02() {
            // given
            int invalidPrice = -1;
            Menu request = MenuFixture.createMenuRequest(invalidPrice);

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴는 미리 등록된 메뉴 그룹에 속해야 한다.")
        @Test
        void test03() {
            // given
            UUID unregisteredMenuGroupId = UUID.randomUUID();

            Product product = productRepository.save(ProductFixture.FRIED_CHICKEN.get());
            Menu request = MenuFixture.createMenuRequest(unregisteredMenuGroupId, product.getId());

            // when & then
            assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴는 하나 이상의 메뉴 상품을 가진다.")
        @Test
        void test04() {
            // given
            MenuProduct[] noMenuProducts = new MenuProduct[0];

            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Menu request = MenuFixture.createMenuRequest(menuGroup.getId(), noMenuProducts);

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴는 존재하지 않는 상품을 메뉴 상품으로 가질 수 없다.")
        @Test
        void test05() {
            // given
            UUID unregisteredProductId = UUID.randomUUID();

            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Menu request = MenuFixture.createMenuRequest(menuGroup.getId(), unregisteredProductId);

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴 상품의 수량은 0 이상이다.")
        @Test
        void test06() {
            // given
            int invalidMenuProductQuantity = -1;

            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Product product = productRepository.save(ProductFixture.FRIED_CHICKEN.get());
            Menu request = MenuFixture.createMenuRequest(
                menuGroup.getId(),
                MenuProductFixture.request(product.getId(), invalidMenuProductQuantity)
            );

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴 가격은 메뉴 상품 가격의 총합보다 클 수 없다.")
        @Test
        void test07() {
            // given
            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Product product = productRepository.save(ProductFixture.FRIED_CHICKEN.get());
            int invalidMenuPrice = product.getPrice().add(BigDecimal.ONE).intValue();

            Menu request = MenuFixture.createMenuRequest(invalidMenuPrice, menuGroup.getId(), product.getId());

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }

        @DisplayName("메뉴 이름은 비어 있을 수 없고, 비속어를 포함 할 수 없다.")
        @ParameterizedTest
        @ValueSource(strings = {"욕설이 포함됨", "비속어가 포함됨"})
        @NullAndEmptySource
        void test08(String invalidName) {
            // given
            MenuGroup menuGroup = menuGroupRepository.save(MenuGroupFixture.CHICKEN);
            Product product = productRepository.save(ProductFixture.FRIED_CHICKEN.get());

            Menu request = MenuFixture.createMenuRequest(invalidName, menuGroup.getId(), product.getId());

            // when
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.create(request));
        }
    }

    @DisplayName("메뉴 가격 변경 테스트")
    @Nested
    class ChangePriceTest {

        @DisplayName("메뉴의 가격을 변경 할 수 있다.")
        @Test
        void test01() {
            // given
            Menu menu = menuRepository.save(MenuFixture.ONE_FRIED_CHICKEN.get());
            Menu request = MenuFixture.changePriceRequest(5000);

            // when
            Menu actual = testTarget.changePrice(menu.getId(), request);

            // then
            assertThat(actual.getPrice()).isEqualTo(request.getPrice());
        }

        @DisplayName("메뉴 가격은 0원 이상이어야 한다.")
        @ParameterizedTest
        @MethodSource("kitchenpos.application.MenuServiceTest#provideNegativeAndNullPrice")
        void test02(BigDecimal invalidPrice) {
            // given
            Menu menu = menuRepository.save(MenuFixture.ONE_FRIED_CHICKEN.get());
            Menu request = MenuFixture.changePriceRequest(invalidPrice);

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.changePrice(menu.getId(), request));
        }

        @DisplayName("존재하지 않는 메뉴의 가격을 변경 할 수 없다.")
        @Test
        void test03() {
            // given
            UUID unregisteredMenuId = UUID.randomUUID();
            Menu request = MenuFixture.changePriceRequest(5000);

            // when & then
            assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> testTarget.changePrice(unregisteredMenuId, request));
        }

        @DisplayName("메뉴의 가격은 메뉴 상품 가격의 합보다 클 수 없다.")
        @Test
        void test04() {
            // given
            int invalidPrice = 7000;
            Menu menu = menuRepository.save(MenuFixture.ONE_FRIED_CHICKEN.get());
            Menu request = MenuFixture.changePriceRequest(invalidPrice);

            // when & then
            assertThatIllegalArgumentException()
                .isThrownBy(() -> testTarget.changePrice(menu.getId(), request));
        }
    }

    @DisplayName("메뉴 노출 테스트")
    @Nested
    class DisplayTest {

        @DisplayName("메뉴를 노출 시킬 수 있다.")
        @Test
        void test01() {
            // given
            Menu menu = menuRepository.save(MenuFixture.NO_DISPLAYED_MENU.get());

            // when
            Menu actual = testTarget.display(menu.getId());

            // then
            assertThat(actual.isDisplayed()).isTrue();
        }

        @DisplayName("존재하지 않는 메뉴를 노출 시킬 수 없다.")
        @Test
        void test02() {
            // given
            UUID menuId = UUID.randomUUID();

            // when & then
            assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> testTarget.display(menuId));
        }

        @DisplayName("메뉴 가격이 메뉴 상품 가격의 총합보다 큰 메뉴를 노출 시킬 수 없다.")
        @Test
        void test03() {
            // given
            Menu menu = menuRepository.save(MenuFixture.PRICE_EXCEED_MENU.get());

            // when & then
            assertThatIllegalStateException()
                .isThrownBy(() -> testTarget.display(menu.getId()));
        }
    }

    @DisplayName("메뉴 감추기 테스트")
    @Nested
    class HideTest {

        @DisplayName("메뉴를 감출 수 있다.")
        @Test
        void test01() {
            // given
            Menu menu = menuRepository.save(MenuFixture.DISPLAYED_MENU.get());

            // when
            Menu actual = testTarget.hide(menu.getId());

            // then
            assertThat(actual.isDisplayed()).isFalse();
        }

        @DisplayName("존재하지 않는 메뉴를 감출 수 없다.")
        @Test
        void test02() {
            // given
            UUID menuId = UUID.randomUUID();

            // when & then
            assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> testTarget.hide(menuId));
        }
    }

    @DisplayName("메뉴 목록 조회 테스트")
    @Nested
    class FindAllTest {

        @DisplayName("메뉴 목록을 조회 할 수 있다.")
        @Test
        void test01() {
            // given
            Menu menu1 = menuRepository.save(MenuFixture.ONE_FRIED_CHICKEN.get());
            Menu menu2 = menuRepository.save(MenuFixture.TWO_FRIED_CHICKEN.get());

            // when
            List<Menu> actual = testTarget.findAll();

            // then
            assertThat(actual)
                .anyMatch(menu -> menu.getId().equals(menu1.getId()))
                .anyMatch(menu -> menu.getId().equals(menu2.getId()));
        }
    }

    private static Stream<Arguments> provideNegativeAndNullPrice() {
        return Stream.of(
            Arguments.of(BigDecimal.valueOf(-1)),
            null
        );
    }
}