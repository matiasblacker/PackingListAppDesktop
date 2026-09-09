package com.logistics.packinglist.service;

import com.logistics.packinglist.model.*;
import java.util.List;
import java.util.Map;
import java.io.File;

public class MantenimientoService {
    private final ReceptionApiService receptionApiService = new ReceptionApiService();
    private final ProductFieldApiService productFieldApiService = new ProductFieldApiService();
    private final LocationApiService locationApiService = new LocationApiService();
    private final PackingListApiService packingListApiService = new PackingListApiService();
    private final StockApiService stockApiService = new StockApiService();
    private final ProductApiService productApiService = new ProductApiService();
    private final OrderNoteApiService orderNoteApiService = new OrderNoteApiService();
    private final CompanyApiService companyApiService = new CompanyApiService();
    private final DispatchApiService dispatchApiService = new DispatchApiService();
    private final CarrierApiService carrierApiService = new CarrierApiService();
    private final WarehouseLocationApiService warehouseLocationApiService = new WarehouseLocationApiService();
    private final CustomerApiService customerApiService = new CustomerApiService();
    private final SupplierApiService supplierApiService = new SupplierApiService();
    private final WarehouseApiService warehouseApiService = new WarehouseApiService();
    private final UserApiService userApiService = new UserApiService();
    private final WarehouseZoneApiService warehouseZoneApiService = new WarehouseZoneApiService();
    private final DepositApiService depositApiService = new DepositApiService();

    private static MantenimientoService instance;
    
    public static synchronized MantenimientoService getInstance() {
        if (instance == null) {
            instance = new MantenimientoService();
        }
        return instance;
    }

    public List<ReceptionAnnouncementModel> obtenerAnuncios() throws Exception {
        return receptionApiService.obtenerAnuncios();
    }

    public List<ReceptionAnnouncementModel> obtenerAnunciosPendientes() throws Exception {
        return receptionApiService.obtenerAnunciosPendientes();
    }

    public ReceptionAnnouncementModel crearAnuncio(ReceptionAnnouncementModel model) throws Exception {
        return receptionApiService.crearAnuncio(model);
    }

    public ReceptionAnnouncementModel actualizarAnuncio(String id, ReceptionAnnouncementModel model) throws Exception {
        return receptionApiService.actualizarAnuncio(id, model);
    }

    public void eliminarAnuncio(String id) throws Exception {
        receptionApiService.eliminarAnuncio(id);
    }

    public List<ReceptionModel> obtenerRecepciones() throws Exception {
        return receptionApiService.obtenerRecepciones();
    }

    public ReceptionModel crearRecepcion(ReceptionModel model) throws Exception {
        return receptionApiService.crearRecepcion(model);
    }

    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String warehouseId, String supplierId) throws Exception {
        return productFieldApiService.obtenerDefinicionesCampos(companyId, null, warehouseId, supplierId);
    }

    public List<ProductFieldDefinitionModel> obtenerDefinicionesCampos(String companyId, String targetEntity, String warehouseId, String supplierId) throws Exception {
        return productFieldApiService.obtenerDefinicionesCampos(companyId, targetEntity, warehouseId, supplierId);
    }

    public ProductFieldDefinitionModel crearDefinicionCampo(ProductFieldDefinitionModel model) throws Exception {
        return productFieldApiService.crearDefinicionCampo(model);
    }

    public ProductFieldDefinitionModel actualizarDefinicionCampo(String id, ProductFieldDefinitionModel model) throws Exception {
        return productFieldApiService.actualizarDefinicionCampo(id, model);
    }

    public void eliminarDefinicionCampo(String id) throws Exception {
        productFieldApiService.eliminarDefinicionCampo(id);
    }

    public List<NotificationModel> obtenerNotificacionesFiltradas(String entityType, String startDate, String endDate, int page, int size) throws Exception {
        return productFieldApiService.obtenerNotificacionesFiltradas(entityType, startDate, endDate, false, page, size);
    }

    public List<NotificationModel> obtenerNotificacionesFiltradas(String entityType, String startDate, String endDate, boolean onlyUndismissed, int page, int size) throws Exception {
        return productFieldApiService.obtenerNotificacionesFiltradas(entityType, startDate, endDate, onlyUndismissed, page, size);
    }

    public void descartarNotificacion(String id) throws Exception {
        productFieldApiService.descartarNotificacion(id);
    }

    public void descartarTodasNotificaciones() throws Exception {
        productFieldApiService.descartarTodasNotificaciones();
    }

    public List<RegionModel> obtenerRegiones() throws Exception {
        return locationApiService.obtenerRegiones();
    }

    public List<PackingList> obtenerPackingLists() throws Exception {
        return packingListApiService.obtenerPackingLists();
    }

    public PackingList crearPackingList(PackingList pl) throws Exception {
        return packingListApiService.crearPackingList(pl);
    }

    public PackingList actualizarPackingList(String id, PackingList pl) throws Exception {
        return packingListApiService.actualizarPackingList(id, pl);
    }

    public void eliminarPackingList(String id) throws Exception {
        packingListApiService.eliminarPackingList(id);
    }

    public List<StockModel> obtenerStocks() throws Exception {
        return stockApiService.obtenerStocks();
    }

    public List<StockModel> obtenerStocksPorProducto(String productId) throws Exception {
        return stockApiService.obtenerStocksPorProducto(productId);
    }

    public List<StockModel> obtenerStocksPorUbicacion(String locationId) throws Exception {
        return stockApiService.obtenerStocksPorUbicacion(locationId);
    }

    public StockModel ajustarStock(String productId, String locationId, int cantidad) throws Exception {
        return stockApiService.ajustarStock(productId, locationId, cantidad);
    }

    public void transferirStock(String productId, String fromLocationId, String toLocationId, int cantidad, String tipoStock) throws Exception {
        stockApiService.transferirStock(productId, fromLocationId, toLocationId, cantidad, tipoStock);
    }

    public void eliminarStock(String id) throws Exception {
        stockApiService.eliminarStock(id);
    }

    public List<ProductModel> obtenerProductos() throws Exception {
        return productApiService.obtenerProductos();
    }

    public ProductModel crearProducto(ProductModel product) throws Exception {
        return productApiService.crearProducto(product);
    }

    public ProductModel actualizarProducto(String id, ProductModel product) throws Exception {
        return productApiService.actualizarProducto(id, product);
    }

    public void eliminarProducto(String id) throws Exception {
        productApiService.eliminarProducto(id);
    }

    public List<OrderNoteModel> obtenerNotasPedido() throws Exception {
        return orderNoteApiService.obtenerNotasPedido();
    }

    public String obtenerSiguienteFolio() throws Exception {
        return orderNoteApiService.obtenerSiguienteFolio();
    }

    public OrderNoteModel crearNotaPedido(OrderNoteModel note) throws Exception {
        return orderNoteApiService.crearNotaPedido(note);
    }

    public OrderNoteModel actualizarNotaPedido(String id, OrderNoteModel note) throws Exception {
        return orderNoteApiService.actualizarNotaPedido(id, note);
    }

    public void eliminarNotaPedido(String id, String motivo) throws Exception {
        orderNoteApiService.eliminarNotaPedido(id, motivo);
    }

    public OrderNoteModel procesarBackorderABoNP(String boId, Map<String, Integer> quantityMap) throws Exception {
        return orderNoteApiService.procesarBackorderABoNP(boId, quantityMap);
    }

    public List<BackOrderModel> obtenerBackOrders() throws Exception {
        return orderNoteApiService.obtenerBackOrders();
    }

    public BackOrderModel obtenerBackOrderPorId(String id) throws Exception {
        return orderNoteApiService.obtenerBackOrderPorId(id);
    }

    public List<BackOrderHistoryModel> obtenerHistorialBackOrder(String id) throws Exception {
        return orderNoteApiService.obtenerHistorialBackOrder(id);
    }

    public OrderNoteModel procesarBackOrder(String id, Map<String, Integer> quantityMap) throws Exception {
        return orderNoteApiService.procesarBackOrder(id, quantityMap);
    }

    public BackOrderModel anularBackOrder(String id, String motivo) throws Exception {
        return orderNoteApiService.anularBackOrder(id, motivo);
    }

    public void registrarEventoTracking(String orderNoteId, String trackingNumber, String estado, String descripcion, String ubicacionNombre, Double latitud, Double longitud, String nombreCourier, String numeroSeguimientoCourier, String fotosUrls) throws Exception {
        orderNoteApiService.registrarEventoTracking(orderNoteId, trackingNumber, estado, descripcion, ubicacionNombre, latitud, longitud, nombreCourier, numeroSeguimientoCourier, fotosUrls);
    }

    public void registrarEventoTracking(String orderNoteId, String trackingNumber, String estado, String descripcion, String ubicacionNombre, Double latitud, Double longitud, String nombreCourier, String numeroSeguimientoCourier, String fotosUrls, String retiradoPorNombre, String retiradoPorRut, String retiradoPorPatente, String fechaHoraISO) throws Exception {
        orderNoteApiService.registrarEventoTracking(orderNoteId, trackingNumber, estado, descripcion, ubicacionNombre, latitud, longitud, nombreCourier, numeroSeguimientoCourier, fotosUrls, retiradoPorNombre, retiradoPorRut, retiradoPorPatente, fechaHoraISO);
    }

    public String subirFotoTracking(String trackingNumber, File archivoFoto) throws Exception {
        return orderNoteApiService.subirFotoTracking(trackingNumber, archivoFoto);
    }

    public void consolidarTrackings(List<String> orderNoteIds) throws Exception {
        orderNoteApiService.consolidarTrackings(orderNoteIds);
    }

    public void registrarImpresionPicking(String noteId) throws Exception {
        orderNoteApiService.registrarImpresionPicking(noteId);
    }

    public List<com.logistics.packinglist.model.TrackingEventModel> obtenerEventosTracking(String noteId) throws Exception {
        return orderNoteApiService.obtenerEventosTracking(noteId);
    }

    public List<com.logistics.packinglist.model.OrderNoteModel> obtenerNotasPedidoConsolidadas(String masterTracking) throws Exception {
        return orderNoteApiService.obtenerNotasPedidoConsolidadas(masterTracking);
    }

    public List<CompanyModel> obtenerEmpresas() throws Exception {
        return companyApiService.obtenerEmpresas();
    }

    public CompanyModel obtenerEmpresaPorId(String id) throws Exception {
        return companyApiService.obtenerEmpresaPorId(id);
    }

    public CompanyModel crearEmpresa(CompanyModel company) throws Exception {
        return companyApiService.crearEmpresa(company);
    }

    public CompanyModel actualizarEmpresa(String id, CompanyModel company) throws Exception {
        return companyApiService.actualizarEmpresa(id, company);
    }

    public void eliminarEmpresa(String id) throws Exception {
        companyApiService.eliminarEmpresa(id);
    }

    public String subirLogo(String companyName, String base64Image, String extension) throws Exception {
        return companyApiService.subirLogo(companyName, base64Image, extension);
    }

    public List<DispatchModel> obtenerDespachos() throws Exception {
        return dispatchApiService.obtenerDespachos();
    }

    public DispatchModel crearDespacho(DispatchModel dispatch) throws Exception {
        return dispatchApiService.crearDespacho(dispatch);
    }

    public DispatchModel actualizarDespacho(String id, DispatchModel dispatch) throws Exception {
        return dispatchApiService.actualizarDespacho(id, dispatch);
    }

    public void eliminarDespacho(String id) throws Exception {
        dispatchApiService.eliminarDespacho(id);
    }

    public List<com.logistics.packinglist.model.CarrierModel> obtenerCarriers() throws Exception {
        return carrierApiService.obtenerCarriers();
    }

    public com.logistics.packinglist.model.CarrierModel crearCarrier(com.logistics.packinglist.model.CarrierModel carrier) throws Exception {
        return carrierApiService.crearCarrier(carrier);
    }

    public com.logistics.packinglist.model.CarrierModel actualizarCarrier(String id, com.logistics.packinglist.model.CarrierModel carrier) throws Exception {
        return carrierApiService.actualizarCarrier(id, carrier);
    }

    public void eliminarCarrier(String id) throws Exception {
        carrierApiService.eliminarCarrier(id);
    }

    public List<LocationModel> obtenerUbicaciones() throws Exception {
        return warehouseLocationApiService.obtenerUbicaciones();
    }

    public List<LocationModel> obtenerUbicacionesPorBodega(String warehouseId) throws Exception {
        return warehouseLocationApiService.obtenerUbicacionesPorBodega(warehouseId);
    }

    public LocationModel crearUbicacion(LocationModel location) throws Exception {
        return warehouseLocationApiService.crearUbicacion(location);
    }

    public LocationModel actualizarUbicacion(String id, LocationModel location) throws Exception {
        return warehouseLocationApiService.actualizarUbicacion(id, location);
    }

    public void eliminarUbicacion(String id) throws Exception {
        warehouseLocationApiService.eliminarUbicacion(id);
    }

    public List<LocationModel> obtenerUbicacionesPorZona(String zoneId) throws Exception {
        return warehouseLocationApiService.obtenerUbicacionesPorZona(zoneId);
    }

    public List<LocationModel> generarUbicacionesEnLote(Map<String, Object> body) throws Exception {
        return warehouseLocationApiService.generarUbicacionesEnLote(body);
    }

    public List<CustomerModel> obtenerClientes() throws Exception {
        return customerApiService.obtenerClientes();
    }

    public CustomerModel crearCliente(CustomerModel customer) throws Exception {
        return customerApiService.crearCliente(customer);
    }

    public CustomerModel actualizarCliente(String id, CustomerModel customer) throws Exception {
        return customerApiService.actualizarCliente(id, customer);
    }

    public void eliminarCliente(String id) throws Exception {
        customerApiService.eliminarCliente(id);
    }

    public List<SupplierModel> obtenerProveedores() throws Exception {
        return supplierApiService.obtenerProveedores();
    }

    public SupplierModel crearProveedor(SupplierModel supplier) throws Exception {
        return supplierApiService.crearProveedor(supplier);
    }

    public SupplierModel actualizarProveedor(String id, SupplierModel supplier) throws Exception {
        return supplierApiService.actualizarProveedor(id, supplier);
    }

    public void eliminarProveedor(String id) throws Exception {
        supplierApiService.eliminarProveedor(id);
    }

    public List<WarehouseModel> obtenerBodegas() throws Exception {
        return warehouseApiService.obtenerBodegas();
    }

    public WarehouseModel obtenerBodegaPorId(String id) throws Exception {
        return warehouseApiService.obtenerBodegaPorId(id);
    }

    public WarehouseModel crearBodega(WarehouseModel warehouse) throws Exception {
        return warehouseApiService.crearBodega(warehouse);
    }

    public WarehouseModel actualizarBodega(String id, WarehouseModel warehouse) throws Exception {
        return warehouseApiService.actualizarBodega(id, warehouse);
    }

    public void eliminarBodega(String id) throws Exception {
        warehouseApiService.eliminarBodega(id);
    }

    public List<UserModel> obtenerUsuarios() throws Exception {
        return userApiService.obtenerUsuarios();
    }

    public UserModel crearUsuario(String email, String password, String nombre, String apellido, String role, String warehouseId, String companyId) throws Exception {
        return userApiService.crearUsuario(email, password, nombre, apellido, role, warehouseId, companyId);
    }

    public UserModel actualizarUsuario(String id, String nombre, String apellido, String role, String warehouseId, String estado) throws Exception {
        return userApiService.actualizarUsuario(id, nombre, apellido, role, warehouseId, estado);
    }

    public void eliminarUsuario(String id) throws Exception {
        userApiService.eliminarUsuario(id);
    }

    public List<com.logistics.packinglist.model.WarehouseZoneModel> obtenerZonasPorBodega(String warehouseId) throws Exception {
        return warehouseZoneApiService.obtenerZonasPorBodega(warehouseId);
    }

    public com.logistics.packinglist.model.WarehouseZoneModel crearZona(com.logistics.packinglist.model.WarehouseZoneModel zone) throws Exception {
        return warehouseZoneApiService.crearZona(zone);
    }

    public com.logistics.packinglist.model.WarehouseZoneModel actualizarZona(String id, com.logistics.packinglist.model.WarehouseZoneModel zone) throws Exception {
        return warehouseZoneApiService.actualizarZona(id, zone);
    }

    public void eliminarZona(String id) throws Exception {
        warehouseZoneApiService.eliminarZona(id);
    }


    public String subirImagenProducto(String warehouseId, String productId, File file) throws Exception {
        return productApiService.subirImagenProducto(warehouseId, productId, file);
    }



    public List<com.logistics.packinglist.model.DepositModel> obtenerDepositos() throws Exception {
        return depositApiService.obtenerDepositos();
    }

    public com.logistics.packinglist.model.DepositModel crearDeposito(com.logistics.packinglist.model.DepositModel model) throws Exception {
        return depositApiService.crearDeposito(model);
    }

    public com.logistics.packinglist.model.DepositModel actualizarDeposito(String id, com.logistics.packinglist.model.DepositModel model) throws Exception {
        return depositApiService.actualizarDeposito(id, model);
    }

    public void eliminarDeposito(String id) throws Exception {
        depositApiService.eliminarDeposito(id);
    }

    public List<WarehouseModel> obtenerBodegasPorDeposito(String depositId) throws Exception {
        return depositApiService.obtenerBodegasPorDeposito(depositId);
    }
}
