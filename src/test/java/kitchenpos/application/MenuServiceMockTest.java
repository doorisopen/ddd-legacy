package kitchenpos.application;

import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.utils.MenuGroupTestRepository;
import kitchenpos.utils.MenuTestRepository;
import kitchenpos.utils.ProductTestRepository;
import kitchenpos.utils.PurgomalumTestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static kitchenpos.fixture.MenuFixture.*;
import static kitchenpos.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MenuServiceMockTest {
    @Mock
    private MenuTestRepository menuRepository;
    @Spy
    private MenuGroupTestRepository menuGroupRepository;
    @Spy
    private ProductTestRepository productRepository;
    @Mock
    private PurgomalumTestClient purgomalumTestClient;
    @InjectMocks
    private MenuService menuService;

    @DisplayName("메뉴를 생성한다")
    @Nested
    class CreateTest {
        @DisplayName("생성을 성공한다")
        @Test
        void create() {
            //given
            when(purgomalumTestClient.containsProfanity(any())).thenReturn(false);
            when(menuRepository.save(any())).thenReturn(메뉴A);

            //when
            Menu savedMenu = menuService.create(메뉴A);

            //then
            then(purgomalumTestClient).should(times(1)).containsProfanity(any());
            then(menuRepository).should(times(1)).save(any());
        }

        @DisplayName("메뉴이 존재하지 않으면 메뉴 생성을 실패한다")
        @Test
        void create_menuGroup_exception() {
            //given
            Menu menu = 메뉴_생성("담당자 실수로 태어난 메뉴", BigDecimal.valueOf(1), false, UUID.randomUUID(), 상품_목록_조회());

            //when
            //then
            assertThatThrownBy(() -> menuService.create(menu)).isInstanceOf(NoSuchElementException.class);
        }

        @DisplayName("메뉴 가격이 음수이면 메뉴 생성을 실패한다")
        @Test
        void create_price_exception() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("담당자 실수로 태어난 메뉴", BigDecimal.valueOf(-1), false, 메뉴_그룹A.getId(), products);

            //when
            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(menu));
        }

        @DisplayName("메뉴 이름이 빈 값이면 메뉴 생성을 실패한다")
        @Test
        void create_name_exception() {
            //given
            Menu menu = 메뉴_생성(null, BigDecimal.valueOf(10_000), false, 메뉴_그룹A.getId(), 상품_목록_조회());

            //when
            //then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> menuService.create(menu));
            then(purgomalumTestClient).should(never()).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }

        @DisplayName("메뉴 이름이 부적절한 내용이 포함된 경우 메뉴 생성을 실패한다")
        @Test
        void create_profanity_name_exception() {
            //given
            Menu menu = 메뉴_생성("gubun", BigDecimal.valueOf(10_000), false, 메뉴_그룹A.getId(), 상품_목록_조회());

            //when
            when(purgomalumTestClient.containsProfanity(any())).thenReturn(true);

            //then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> menuService.create(menu));
            then(purgomalumTestClient).should(times(1)).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }

        @DisplayName("메뉴 가격이 포함된 상품 가격의 합을 초과하면 메뉴 생성을 실패한다")
        @Test
        void create_price_sum_exception() {
            //given
            Menu menu = 메뉴_생성("낚시메뉴", BigDecimal.valueOf(100_000), false, 메뉴_그룹A.getId(), 상품_목록_조회());

            //when
            //then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> menuService.create(menu));
            then(purgomalumTestClient).should(never()).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }

        @DisplayName("메뉴 상품의 수량이 0 미만이면 메뉴 생성을 실패한다")
        @Test
        void create_product_quantity_exception() {
            //given
            MenuProduct 메뉴_상품A = 메뉴_상품_생성(상품A, -1L);
            Menu menu = 메뉴_생성("낚시메뉴", BigDecimal.valueOf(10_000), false, 메뉴_그룹A.getId(), 메뉴_상품A);

            //when
            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(menu));
            then(purgomalumTestClient).should(never()).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }

        @DisplayName("메뉴 상품을 선택하지 않으면 메뉴 생성을 실패한다")
        @Test
        void create_product_empty_exception() {
            //given
            Menu menu = 메뉴_생성("낚시메뉴", BigDecimal.valueOf(10_000), false, 메뉴_그룹A.getId(), Collections.emptyList());

            //when
            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(menu));
            then(purgomalumTestClient).should(never()).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }

        @DisplayName("선택한 메뉴 상품이 존재하지 않는 상품인 경우 메뉴 생성을 실패한다")
        @Test
        void create_product_size_exception() {
            //given
            MenuProduct 메뉴_상품A = 메뉴_상품_생성(상품A, 1L);
            Menu menu = 메뉴_생성("낚시메뉴", BigDecimal.valueOf(10_000), false, 메뉴_그룹A.getId(), 메뉴_상품A);

            //when
            when(productRepository.findAllByIdIn(any())).thenReturn(List.of(상품A, 상품B));

            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(menu));
            then(productRepository).should(times(1)).findAllByIdIn(any());
            then(purgomalumTestClient).should(never()).containsProfanity(any());
            then(menuRepository).should(never()).save(any());
        }
    }

    @DisplayName("메뉴 가격을 변경한다")
    @Nested
    class ChangePriceTest {
        @DisplayName("변경을 성공한다")
        @Test
        void changePrice() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.ZERO, false, 메뉴_그룹A.getId(), products);

            when(menuRepository.findById(any())).thenReturn(Optional.of(메뉴A));

            //when
            Menu changedMenu = menuService.changePrice(UUID.randomUUID(), menu);

            //then
            assertThat(changedMenu.getPrice()).isEqualTo(BigDecimal.ZERO);
            then(menuRepository).should(times(1)).findById(any());
        }

        @DisplayName("가격은 0원 미만이면 변경을 실패한다")
        @Test
        void changePrice_price_exception() {
            //given
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.valueOf(-1), false, UUID.randomUUID(), Collections.emptyList());

            //when
            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.changePrice(UUID.randomUUID(), menu));
        }

        @DisplayName("존재하지 않는 메뉴의 가격은 변경할 수 없다")
        @Test
        void changePrice_menu_exception() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.ZERO, false, UUID.randomUUID(), products);

            when(menuRepository.findById(any())).thenReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> menuService.changePrice(UUID.randomUUID(), menu)).isInstanceOf(NoSuchElementException.class);
            then(menuRepository).should(times(1)).findById(any());
        }

        @DisplayName("메뉴 가격이 포함된 상품 가격의 합을 초과하면 가격 변경을 실패한다")
        @Test
        void changePrice_price_sum_exception() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.valueOf(100_000), false, UUID.randomUUID(), products);

            when(menuRepository.findById(any())).thenReturn(Optional.of(메뉴A));

            //when
            //then
            assertThatIllegalArgumentException().isThrownBy(() -> menuService.changePrice(UUID.randomUUID(), menu));
            then(menuRepository).should(times(1)).findById(any());
        }
    }

    @DisplayName("메뉴 노출여부를 변경한다")
    @Nested
    class DisplayTest {
        @DisplayName("노출 상태로 변경한다")
        @Test
        void display() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.valueOf(10_000), false, UUID.randomUUID(), products);

            when(menuRepository.findById(any())).thenReturn(Optional.of(menu));

            //when
            Menu changedMenu = menuService.display(UUID.randomUUID());

            //then
            assertThat(changedMenu.isDisplayed()).isTrue();
            then(menuRepository).should(times(1)).findById(any());
        }

        @DisplayName("존재하지 않는 메뉴의 노출여부를 노출 상태로 변경할 수 없다")
        @Test
        void display_menu_exception() {
            //given
            UUID menuId = UUID.randomUUID();
            when(menuRepository.findById(any())).thenReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> menuService.display(menuId)).isInstanceOf(NoSuchElementException.class);
        }

        @DisplayName("노출 상태로 변경 시, 메뉴 가격이 포함된 상품 가격의 합을 초과하면 노출여부 변경을 실패한다")
        @Test
        void display_price_sum_exception() {
            //given
            List<Product> products = 상품_목록_조회();
            Menu menu = 메뉴_생성("메뉴A", BigDecimal.valueOf(100_000), false, UUID.randomUUID(), products);

            when(menuRepository.findById(any())).thenReturn(Optional.of(menu));

            //when
            //then
            assertThatIllegalStateException().isThrownBy(() -> menuService.display(UUID.randomUUID()));
            then(menuRepository).should(times(1)).findById(any());
        }

        @DisplayName("숨김 상태로 변경한다")
        @Test
        void hide() {
            //given
            UUID menuId = UUID.randomUUID();
            when(menuRepository.findById(any())).thenReturn(Optional.of(메뉴A));

            //when
            Menu hidedMenu = menuService.hide(menuId);

            //then
            assertThat(hidedMenu.isDisplayed()).isFalse();
            then(menuRepository).should(times(1)).findById(menuId);
        }

        @DisplayName("존재하지 않는 메뉴의 노출여부를 숨김 상태로 변경할 수 없다")
        @Test
        void hide_exception() {
            //given
            UUID menuId = UUID.randomUUID();
            when(menuRepository.findById(any())).thenReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> menuService.hide(menuId)).isInstanceOf(NoSuchElementException.class);
        }
    }

    @DisplayName("메뉴 목록을 조회한다")
    @Test
    void findAll() {
        //given
        when(menuRepository.findAll()).thenReturn(Collections.emptyList());

        //when
        List<Menu> menus = menuService.findAll();

        //then
        assertThat(menus).hasSize(0);
        then(menuRepository).should(times(1)).findAll();
    }

    private static List<Product> 상품_목록_조회() {
        return List.of(상품A, 상품B, 상품C);
    }
}
