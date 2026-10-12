package com.ema.practica.infrastructure.util;

import com.ema.practica.infrastructure.devolucion.rest.api.DevolucionController;
import com.ema.practica.infrastructure.devolucion.useCases.DevolucionService;
import com.ema.practica.infrastructure.images.services.ImagenService;
import com.ema.practica.infrastructure.moduloProduct.category.rest.api.CategoryController;
import com.ema.practica.infrastructure.moduloProduct.category.useCases.CategoryService;
import com.ema.practica.infrastructure.moduloProduct.product.rest.api.ProductController;
import com.ema.practica.infrastructure.moduloProduct.product.useCases.ProductService;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.order.rest.api.OrderController;
import com.ema.practica.infrastructure.order.useCases.OrderService;
import com.ema.practica.infrastructure.util.user.rest.api.DeleteUserController;
import com.ema.practica.infrastructure.util.user.rest.api.GetAllUserController;
import com.ema.practica.infrastructure.util.user.useCases.DeleteUserUseCaseImp;
import com.ema.practica.infrastructure.util.user.useCases.ListUserUseCaseImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        OrderController.class, CategoryController.class, ProductController.class,
        DevolucionController.class,
        GetAllUserController.class, DeleteUserController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class SecurityRulesTest {

    @Autowired MockMvc mvc;

    @MockBean Jwt jwt;
    @MockBean UserDetailsService userDetailsService;
    @MockBean OrderService orderService;
    @MockBean DevolucionService devolucionService;
    @MockBean CategoryService categoryService;
    @MockBean ProductService productService;
    @MockBean ImagenService imagenService;
    @MockBean ProductVariantRepository variantRepository;
    @MockBean ListUserUseCaseImp listUserUseCaseImp;
    @MockBean DeleteUserUseCaseImp deleteUserUseCaseImp;

    // ── Sin login ──────────────────────────────────────────────────────────
    @Test
    void sinToken_rechazado() throws Exception {
        mvc.perform(get("/api/categorias")).andExpect(status().isForbidden());
    }

    // ── COLABORADOR ────────────────────────────────────────────────────────
    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_puedeConsultarCatalogo() throws Exception {
        when(categoryService.listarTodas()).thenReturn(List.of());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_puedeListarVentas() throws Exception {
        mvc.perform(get("/api/admin/orders")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_noPuedeCancelarVentas() throws Exception {
        mvc.perform(patch("/api/admin/orders/1/cancelar")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_puedeRegistrarDevoluciones() throws Exception {
        mvc.perform(post("/api/admin/orders/1/devoluciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DEVOLUCION\",\"formaReintegro\":\"EFECTIVO\","
                        + "\"items\":[{\"idDetail\":1,\"quantity\":1}]}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/admin/devoluciones")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void devolucionSinItems_devuelve400() throws Exception {
        mvc.perform(post("/api/admin/orders/1/devoluciones").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"DEVOLUCION\",\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_puedeCancelarConMotivo() throws Exception {
        mvc.perform(patch("/api/admin/orders/1/cancelar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Cobro duplicado\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/admin/orders/1/cancelar")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_noPuedeModificarCatalogo() throws Exception {
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nameCategory\":\"x\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/categorias/1")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/productos/variantes/1/stock")).andExpect(status().isForbidden());
        mvc.perform(put("/api/productos/" + java.util.UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_editarProductoInvalido_devuelve400() throws Exception {
        mvc.perform(put("/api/productos/" + java.util.UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nameProduct\":\"\"}")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaborador_noPuedeGestionarUsuarios() throws Exception {
        mvc.perform(get("/getUser/all")).andExpect(status().isForbidden());
        mvc.perform(delete("/deleteUser/" + java.util.UUID.randomUUID())).andExpect(status().isForbidden());
    }

    // ── ADMIN ──────────────────────────────────────────────────────────────
    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_puedeBorrarCategoria() throws Exception {
        mvc.perform(delete("/api/categorias/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_puedeListarUsuarios() throws Exception {
        when(listUserUseCaseImp.getAll()).thenReturn(List.of());
        mvc.perform(get("/getUser/all")).andExpect(status().isNoContent());
    }

    // ── CORS ───────────────────────────────────────────────────────────────
    @Test
    void cors_permiteFrontendAngular_yRechazaOtros() throws Exception {
        mvc.perform(options("/api/categorias")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
        mvc.perform(options("/api/categorias")
                        .header("Origin", "http://evil.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
