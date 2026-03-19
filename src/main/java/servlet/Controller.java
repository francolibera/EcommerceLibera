package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import logic.FechaHoy;
import logic.Login;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import entities.*;
import data.*;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@MultipartConfig
public class Controller extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private Persona persona= new Persona();
    private DataPersona personaDAO=new DataPersona();
    private DataProducto dp = new DataProducto();
    private DataCategoria dc = new DataCategoria();
    private DataCompra dcompra = new DataCompra();
    private DataRol dr = new DataRol();
    private List<Carrito> listaCarrito = new ArrayList<>();
    private Login ctrl= new Login();
    Rol rol= new Rol();
    int item;
    double totalPagar=0.0;
    int cantidad = 1;
    int idp;
    Carrito car;
    Pago pago = new Pago();
    int idcompra;
    int idPago;
    int rpago=0;
     FechaHoy fechaSistema= new FechaHoy();
     private List<Compra> misCompras = new ArrayList<Compra>();
     private List<Pago> pagos = new ArrayList<Pago>();
     private List<Compra> compras = new ArrayList<Compra>();
     private List<DetalleCompra> detalles = new ArrayList<DetalleCompra>();
     private List<Persona> personas = new LinkedList<>();
    public Controller() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "ListarCatalogo";
        }
        processRequest(request, response, accion);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "ListarCatalogo";
        }
        processRequest(request, response, accion);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String accion) throws ServletException, IOException {
        switch (accion) {
            case "ListarCatalogo":
                listarCatalogo(request, response);
                break;
            case "Listar":
                listarProductos(request, response);
                break;
            case "Edit":
                editarProducto(request, response);
                break;
            case "ActualizarProducto":
                actualizarProducto(request, response);
                break;
            case "Guardar":
                guardarProducto(request, response);
                break;
            case "Delete":
            	deleteItemCarrito(request, response);
                break;
            case "ActualizarCantidad":
            	actualizarCantidad(request,response);
            	break;
            case "SignIn":
            	signIn(request, response);
            	break;
            case "SignOut":
            	signOut(request, response);
            break;
            case "Registrar":
            	registerForm(request, response);
                break;
            case "AgregarCarrito":
            	agregarCarrito(request, response);
            	break;
            case "Carrito":
            	carritoDeCompras(request, response);
            	break;
            case "Comprar":
            	generarCompra(request,response);
            	break;
            case "ComprarAhora":
            	comprarAhora(request, response);
            	break;
            case "RealizarPago":
            	realizarPago(request, response);
            	break;
            case "CrearCategoria":
            	addCategoria(request, response);
            	break;
            case "ListarCategorias":
            	listarCategorias(request, response);
            	break;
            case "EditCategoria":
            	editarCategoria(request, response);
            	break;
            case "DeleteCategoria":
            	deleteCategoria(request, response);
            break;
            case "ListarPersonas":
            	listarPersonas(request, response);
            	break;
            case "AsignarRol":
            	asignarRol(request, response);
            	break;
            case "QuitarRol":
            	quitarRol(request, response);
            	break;
            case "DeleteProducto":
            	eliminarProducto(request, response);
            	break;
            case "ToggleHabilitado":
            	toggleHabilitado(request, response);
                break;
            case "GuardarTarjeta":
            	addTarjeta(request, response);
            	break;
            case "AñadirSaldo":
            	agregarSaldo(request, response);
            	break;
            case "MisCompras":
            	listarMiscompras(request, response);
            	break;
            case "Detalles":
            	listarDetalleCompra(request, response);
                break;
            case "ListarCompras":
            	listarCompras(request, response);
                break;
            case "CambiarEstado":
                cambiarEstadoCompra(request, response);
                break;
            case "ResumenDeCompra":
            	resumenCompra(request, response);
            	 break;
            case "ListarPagos":
            	listarPagos(request, response);
            	 break;
            case "BuscarProducto":
            	buscador(request, response);
            	 break;
            default:
                listarCatalogo(request, response);
                break;
        }
    }
    
    private void buscador(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		String nombre = request.getParameter("txtBuscar");
    		List<Producto> productos = dp.getByName(nombre);
            request.setAttribute("productos", productos);
            request.getRequestDispatcher("index.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void listarPagos(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		pagos = dcompra.getAllPagos();
    		request.setAttribute("pagos", pagos);
    		request.getRequestDispatcher("pagosADMIN.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    		request.setAttribute("error", "Ocurrió un error al cargar las compras.");
            request.getRequestDispatcher("pagosADMIN.jsp").forward(request, response);
    	}
    }
    
    private void resumenCompra(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	idcompra = dcompra.getIdDeUltimaCompra();
        List detalle1 = dcompra.getDetalles(idcompra);
        request.setAttribute("myDetalle", detalle1);
        request.getRequestDispatcher("resumenCompra.jsp").forward(request, response);
    }
    
    private void cambiarEstadoCompra(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idcompra = Integer.parseInt(request.getParameter("idcompra"));
            String nuevoEstado = request.getParameter("nuevoEstado");
            Compra estadoNuevo = new Compra();
            estadoNuevo.setId(idcompra);
            estadoNuevo.setEstado(nuevoEstado);
            dcompra.actualizaEstado(estadoNuevo);
            
            response.sendRedirect("Controller?accion=ListarCompras");
        } catch(Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Ocurrió un error al cambiar el estado de la compra.");
            request.getRequestDispatcher("comprasADMIN.jsp").forward(request, response);
        }
    }
    
    private void listarCompras(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		compras = dcompra.getAll();
    		request.setAttribute("compras", compras);
    		request.getRequestDispatcher("comprasADMIN.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    		request.setAttribute("error", "Ocurrió un error al cargar las compras.");
            request.getRequestDispatcher("comprasADMIN.jsp").forward(request, response);
    	}
    }
    
    private void listarDetalleCompra(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		int idcompra = Integer.parseInt(request.getParameter("idcompra"));
    		detalles = dcompra.getDetalles(idcompra);
    		request.setAttribute("detalles", detalles);
    		request.getRequestDispatcher("detalle.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    		request.setAttribute("error", "Ocurrió un error al cargar el detalle.");
            request.getRequestDispatcher("detalle.jsp").forward(request, response);       
    	}
    }
    
    private void listarMiscompras(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		Persona user = (Persona) request.getSession().getAttribute("user");
            if (user != null) {
                int idpersona = user.getId();
                List<Compra> misCompras = dcompra.getMisCompras(idpersona);
                request.setAttribute("misCompras", misCompras);
            }
            request.getRequestDispatcher("compras.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    		request.setAttribute("error", "Ocurrió un error al cargar las compras.");
            request.getRequestDispatcher("compras.jsp").forward(request, response);       
    	}
    }
    
    private void agregarSaldo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    	String numeroTarjeta = request.getParameter("numeroTarjeta");
    	double nuevoSaldo = Double.parseDouble(request.getParameter("saldoAgregar"));
    	Tarjeta tnuevoSaldo = new Tarjeta();
    	tnuevoSaldo = dcompra.GetTarjeta(numeroTarjeta);
    	//System.out.println("Entre");
    	nuevoSaldo = nuevoSaldo + tnuevoSaldo.getSaldo();
    	dcompra.ActualizaSaldo(nuevoSaldo, tnuevoSaldo.getId());
    	response.setContentType("text/plain");
        response.getWriter().write("Nuevo saldo $" + nuevoSaldo + " en la tarjeta " + numeroTarjeta + " agregado exitosamente.");
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void addTarjeta(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		 String numeroTarjeta = request.getParameter("numeroTarjeta");
    		 String nombreTarjeta = request.getParameter("nombreTarjeta");
    		 String fechaTarjeta = request.getParameter("fechaTarjeta");
    		 String codigoCVV = request.getParameter("cvvTarjeta");
    		 double saldoInicial = Double.parseDouble(request.getParameter("saldoTarjeta"));
    		 Tarjeta t = new Tarjeta();
    		 t.setNumero(numeroTarjeta);
    		 t.setNombre(nombreTarjeta);
    		 t.setFecha(fechaTarjeta);
    		 t.setCodigo(codigoCVV);
    		 t.setSaldo(saldoInicial);
    		 dcompra.addTarjeta(t);
    		 request.getRequestDispatcher("Controller?accion=ListarCatalogo").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void toggleHabilitado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            int personaId = Integer.parseInt(request.getParameter("id"));
            boolean estadoActual = Boolean.parseBoolean(request.getParameter("estadoActual"));     
            Persona persona = new Persona();
            persona.setId(personaId);
            persona.setHabilitado(!estadoActual); 
            personaDAO.ToggleHabilitado(persona);
            out.print("SUCCESS: Estado de habilitación cambiado correctamente");
        } catch (Exception e) {
            out.print("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void quitarRol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        
        try {
            int personaId = Integer.parseInt(request.getParameter("id"));
            String rolDescripcion = request.getParameter("rolDescripcion");
            
            Persona persona = new Persona();
            persona.setId(personaId);
            Rol rol = new Rol();
            rol.setDescripcion(rolDescripcion);
            rol = dr.getByDesc(rol);
            
            if (rol != null) {
                dr.removeRol(persona, rol);
                out.print("SUCCESS: Rol quitado correctamente");
            } else {
                out.print("ERROR: Rol no encontrado");
            }
        } catch (Exception e) {
            out.print("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void listarPersonas(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		List<Persona> personas = personaDAO.getAll();
    		 request.setAttribute("personas", personas);
             request.getRequestDispatcher("personasADMIN.jsp").forward(request, response);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    private void asignarRol(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        
        try (PrintWriter out = response.getWriter()) { 
            int personaId = Integer.parseInt(request.getParameter("id"));
            String rolDescripcion = request.getParameter("rolDescripcion");
            
            Persona perNewrol = new Persona();
            perNewrol.setId(personaId);
            
            Rol rol = new Rol();
            rol.setDescripcion(rolDescripcion);
            rol = dr.getByDesc(rol);
            
            perNewrol.addRol(rol);
            dr.añadirRol(perNewrol);
            

            out.write("SUCCESS: Rol asignado correctamente");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("ERROR: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    private void deleteCategoria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		int idCategoria = Integer.parseInt(request.getParameter("id"));
    		dc.delete(idCategoria);
    		request.getRequestDispatcher("Controller?accion=ListarCategorias").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    private void editarCategoria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		int idCategoria = Integer.parseInt(request.getParameter("id"));
    		String nombCatEdit = request.getParameter("nombre");
    		Categoria catEdit = new Categoria();
    		catEdit.setId(idCategoria);
    		catEdit.setNombre(nombCatEdit);
    		dc.update(catEdit);
    		request.getRequestDispatcher("Controller?accion=ListarCategorias").forward(request, response);
    	}catch(Exception e) {
    	e.printStackTrace();
    	}
    }
    
    private void addCategoria(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        try {
            String nombreCat = request.getParameter("txtNom");
            System.out.print(nombreCat);
            if (nombreCat == null || nombreCat.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
            }
            
            Categoria cat = new Categoria();
            Categoria getOne = new Categoria();
            getOne=dc.getByName(nombreCat);
            if (getOne == null) {
            cat.setNombre(nombreCat);
            System.out.print(cat.getNombre());
            dc.add(cat);
            } else {
            	throw new IllegalArgumentException("Ya existe una categoria con este nombre");
            }
            
            

            request.getRequestDispatcher("Controller?accion=ListarCategorias").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ERROR: " + e.getMessage());
            System.out.print("error");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("ERROR: " + e.getMessage());
            e.printStackTrace(); 
            System.out.print( e);
        	}
        }
    
    private void listarCategorias(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        
        try {
            List<Categoria> categorias = dc.getAll(); 
            request.setAttribute("categorias", categorias);
            request.getRequestDispatcher("categoriasCRUD.jsp").forward(request, response);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("ERROR: " + e.getMessage());
            System.out.print( e);
            e.printStackTrace();
        }
    }
    
    private void realizarPago(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Configurar el Access Token de Mercado Pago
            // El token se lee primero de las variables de entorno, y si no existe, de un archivo local .env
            String mpAccessToken = System.getenv("MP_ACCESS_TOKEN");
            
            if (mpAccessToken == null || mpAccessToken.isEmpty()) {
                // Intento de leer de un archivo .env en la ruta local del proyecto
                try {
                    Properties props = new Properties();
                    // Obtiene la ruta del entorno de ejecución (suele ser el directorio base de eclipse/tomcat)
                    // Para mayor compatibilidad en tu entorno de desarrollo:
                    String classPath = getServletContext().getRealPath("/WEB-INF/classes/");
                    if (classPath != null) {
                        java.io.File file = new java.io.File(classPath + "../../../.env");
                        if(file.exists()){
                            try(FileInputStream in = new FileInputStream(file)){
                                props.load(in);
                                mpAccessToken = props.getProperty("MP_ACCESS_TOKEN");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo leer el .env local: " + e.getMessage());
                }
            }
            
            // Fallback final si todo lo demás falla (esto lanzará un error 401 en MP si se llega a ejecutar)
            if (mpAccessToken == null || mpAccessToken.isEmpty()) {
                mpAccessToken = "TU_ACCESS_TOKEN_AQUI"; 
            }
            
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            System.out.println("====== PROCESANDO ITEMS ======");
            System.out.println("Tamaño de listaCarrito: " + (listaCarrito != null ? listaCarrito.size() : "NULA"));
            
            if (listaCarrito == null || listaCarrito.isEmpty()) {
                throw new Exception("El carrito está vacío. No se puede generar el pago.");
            }

            for (Carrito c : listaCarrito) {
                System.out.println("Agregando item: " + c.getNombres() + " | Precio: " + c.getPrecioCompra() + " | Cantidad: " + c.getCantidad());
                // Asegurarse de que el precio no sea 0 o negativo
                double precioUnitario = c.getPrecioCompra();
                if (precioUnitario <= 0) {
                    precioUnitario = 1.0; // Precio mínimo de seguridad
                    System.out.println("ADVERTENCIA: Precio unitario ajustado a 1.0 porque era <= 0");
                }
                
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(c.getNombres() != null ? c.getNombres() : "Producto sin nombre")
                    .quantity(c.getCantidad() > 0 ? c.getCantidad() : 1)
                    .unitPrice(new BigDecimal(precioUnitario))
                    .currencyId("ARS")
                    .build();
                items.add(itemRequest);
            }

            // Generar la URL base dinámicamente
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            
            // ATENCIÓN: Si vas a usar Ngrok u otra URL pública, cambiar aquí.
            // Mientras sea localhost, no uses autoReturn.
            String successUrl = baseUrl + "/Controller?accion=Comprar";
            String pendingUrl = baseUrl + "/Controller?accion=Carrito";
            String failureUrl = baseUrl + "/Controller?accion=Carrito";
            
            System.out.println("Back URL Success: " + successUrl);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .pending(pendingUrl)
                .failure(failureUrl)
                .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                // Volvemos a comentar autoReturn. En cuentas nuevas de prueba, MP exige que 
                // la URL de retorno sea HTTPS público para usar autoReturn.
                // .autoReturn("approved")
                .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            
            rpago = 1; // Para que generarCompra() sepa que se aprobó
            
            // Redirige al usuario al checkout de Mercado Pago
            // USAMOS EL INIT POINT DE PRODUCCIÓN PARA EVITAR LOS BUGS DE SANDBOX.
            // COMO USAMOS UN ACCESS TOKEN DE PRUEBA (APP_USR...), MERCADO PAGO SABRÁ 
            // QUE ES UNA PRUEBA Y NO COBRARÁ NADA, PERO LA INTERFAZ FUNCIONARÁ MEJOR.
            response.sendRedirect(preference.getInitPoint());
            
        } catch (com.mercadopago.exceptions.MPApiException apiException) {
            System.err.println("====== ERROR DE MERCADO PAGO ======");
            System.err.println("Status Code: " + apiException.getStatusCode());
            if (apiException.getApiResponse() != null) {
                System.err.println("Contenido de la respuesta: " + apiException.getApiResponse().getContent());
            } else {
                System.err.println("La respuesta de la API es nula. Mensaje: " + apiException.getMessage());
            }
            System.err.println("===================================");
            apiException.printStackTrace();
            
            String errorMsg = (apiException.getApiResponse() != null) ? apiException.getApiResponse().getContent() : apiException.getMessage();
            request.setAttribute("error", "Error en Mercado Pago: " + errorMsg);
            // Intentar redirigir, si falla, al menos sabemos que entramos acá
            try {
                request.getRequestDispatcher("carrito.jsp").forward(request, response);
            } catch (Exception ex) {
                System.err.println("Error al redirigir: " + ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al crear la preferencia de pago de Mercado Pago.");
            request.getRequestDispatcher("carrito.jsp").forward(request, response);
        }
    }

    
    private void signOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false); 
        if (session != null) {
            session.invalidate(); 
        }

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("user")) {
                    cookie.setMaxAge(0); 
                    cookie.setPath("/");
                    response.addCookie(cookie); 
                }
            }
        }

        response.sendRedirect("Controller?accion=ListarCatalogo");
    }
	

	private void signIn(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
    	 try {
    	        String email = request.getParameter("email");
    	        String password = request.getParameter("password");

    	        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
    	            request.setAttribute("mensajeError", "Email y contraseña son obligatorios.");
    	            request.getRequestDispatcher("index.jsp").forward(request, response);
    	            return;
    	        }

                if (persona == null) {
                    persona = new Persona();
                }
    	        persona.setEmail(email);
    	        persona.setPassword(password);

    	        persona = ctrl.validate(persona);

    	        if (persona != null && persona.isHabilitado()) {
    	            request.getSession().setAttribute("user", persona);
    	            response.sendRedirect("Controller?accion=ListarCatalogo");
    	            System.out.println("Persona logueada");
    	            System.out.println(persona.getApellido());
    	        } else {
    	            request.setAttribute("mensajeError", "Email o contraseña incorrectos, o usuario no habilitado.");
    	            request.getRequestDispatcher("index.jsp").forward(request, response);
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        request.setAttribute("mensajeError", "Ocurrió un error durante el inicio de sesión.");
    	        try {
					request.getRequestDispatcher("index.jsp").forward(request, response);
				} catch (ServletException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
    	    }
	}

	private void generarCompra(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    	// Simulamos el ID de la tarjeta de Mercado Pago en la DB usando una tarjeta existente.
    	// Usaremos el ID 1 (Franco Libera) que vemos que existe en la base de datos
    	// para poder registrar el pago sin errores de Foreign Key.
    	int idTarjetaMercadoPago = 1; 
    	
    	if(persona.getId()!=0 && listaCarrito.size()!=0 && rpago==1) {
    		if (totalPagar>0.0) {
    			
    			// 1. Registramos el pago en nuestra base de datos para obtener el idPago
    			rpago = dcompra.Pagar(totalPagar, idTarjetaMercadoPago);
    			
    			idPago= dcompra.GetIdDelUltimoPago();
    			
    			Compra co = new Compra();
    			co.setPersona(persona);
    			co.setFecha(fechaSistema.FechaBD());
    			co.setMonto(totalPagar);
    			co.setIdpago(idPago);
    			co.setEstado("En espera");
    			dcompra.GenerarCompra(co);
    			
    			idcompra = dcompra.getIdDeUltimaCompra();
    			for (int i=0;i<listaCarrito.size();i++) {
    				DetalleCompra detalle = new DetalleCompra();
    				detalle.setIdcompra(idcompra);
    				detalle.setIdproducto(listaCarrito.get(i).getIdProducto());
    				detalle.setCantidad(listaCarrito.get(i).getCantidad());
    				detalle.setPrecioCompra(listaCarrito.get(i).getPrecioCompra());
    				dcompra.guardarDetalle_compra(detalle);
    			}
    			
    			for (Carrito p :listaCarrito ) {
    				Producto pro = new Producto();
    				pro.setId(p.getIdProducto());
    				pro.setStock(p.getCantidad());
    				dp.ActualizarStock(pro);
    			}
    			listaCarrito= new ArrayList<>();
    			rpago = 0; // Reseteamos la bandera de pago
    			misCompras = dcompra.getMisCompras(persona.getId());
    			request.getRequestDispatcher("Controller?accion=ResumenDeCompra").forward(request, response);
    			
    		} else {
    			request.getRequestDispatcher("Controller?accion=ListarCatalogo").forward(request, response);
    		}
    	} else {
    		request.getRequestDispatcher("Controller?accion=Carrito").forward(request, response);
    	}
    }
    
	private void registerForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		Rol rol = new Rol();
    	rol.setId(2);
    	String tipoDoc = request.getParameter("tipo_doc");
        String numeroDoc = request.getParameter("numero_doc");
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String email = request.getParameter("email");
        String telefono = request.getParameter("telefono");
        String password = request.getParameter("password");
        Documento doc= new Documento();
        doc.setNro(numeroDoc);
        doc.setTipo(tipoDoc);
        
        Persona persona = new Persona();
        try {
            persona.setDocumento(doc);
            persona.setNombre(nombre);
            persona.setApellido(apellido);
            persona.setEmail(email);
            persona.setTel(telefono);
            persona.setPassword(password);
            persona.setHabilitado(true);
            persona.addRol(rol);
            personaDAO.add(persona);
            request.setAttribute("mensaje", "Registro exitoso");
            request.getRequestDispatcher("Controller?accion=ListarCatalogo").forward(request, response);
        }catch(Exception e) {
        	e.printStackTrace();
            request.setAttribute("mensajeError", "Ocurrió un error durante el registro");
        }
	}
    	
	private void deleteItemCarrito(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int idproducto=Integer.parseInt(request.getParameter("idp"));
        for (int i=0; i<listaCarrito.size(); i++) {
        	if(listaCarrito.get(i).getIdProducto()==idproducto) {
        		listaCarrito.remove(i);
        	}
        }
        response.setContentType("text/plain");
        response.getWriter().write("Producto eliminado con éxito");

	}

	private void listarCatalogo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Producto> catalogo = dp.getAll();
        request.setAttribute("productos", catalogo);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    private void listarProductos(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Producto> productos = dp.getAll();
        request.setAttribute("productos", productos);
        request.getRequestDispatcher("prodcutosCRUD.jsp").forward(request, response);
    }

    private void editarProducto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Producto producto = dp.getById(id);
            if (producto != null) {
                request.setAttribute("producto", producto);
                request.getRequestDispatcher("editarProducto.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Producto no encontrado.");
                listarProductos(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID de producto inválido.");
            listarProductos(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar la solicitud de edición.");
            listarProductos(request, response);
        }
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idProducto = Integer.parseInt(request.getParameter("id"));
            String nombre = request.getParameter("txtNom");
            String descripcion = request.getParameter("txtDesc");
            double precio = Double.parseDouble(request.getParameter("numbPrecio"));
            int stock = Integer.parseInt(request.getParameter("numbStock"));
            String nombreCat = request.getParameter("nomCat");

            Producto productoActualizar = dp.getById(idProducto);
            productoActualizar.setNombre(nombre);
            productoActualizar.setDescripcion(descripcion);
            productoActualizar.setPrecio(precio);
            productoActualizar.setStock(stock);

            Categoria cat = dc.getByName(nombreCat);
            if (cat != null) {
                productoActualizar.setCat(cat);
            }

            Part filePart = request.getPart("fileFoto");
            if (filePart != null && filePart.getSize() > 0) {
                if (filePart.getContentType().startsWith("image/")) {
                    InputStream inputStream = filePart.getInputStream();
                    productoActualizar.setFoto(inputStream);
                } else {
                    request.setAttribute("error", "Por favor, sube solo archivos de imagen.");
                    request.getRequestDispatcher("editarProducto.jsp").forward(request, response);
                    return;
                }
            }

            dp.update(productoActualizar);
            listarProductos(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Ocurrió un error al actualizar el producto.");
            request.getRequestDispatcher("editarProducto.jsp").forward(request, response);
        }
    }

    private void guardarProducto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String nom = request.getParameter("txtNom");
            Part part = request.getPart("fileFoto");
            InputStream inputStream = part.getInputStream(); 
            String desc = request.getParameter("txtDesc");
            double precio = Double.parseDouble(request.getParameter("numbPrecio"));
            int stock = Integer.parseInt(request.getParameter("numbStock"));
            String nombreCat = request.getParameter("nomCat");

            Categoria cat = dc.getByName(nombreCat);
            if (cat != null) {
                Producto p = new Producto();
                p.setNombre(nom);
                p.setFoto(inputStream);
                p.setDescripcion(desc);
                p.setPrecio(precio);
                p.setStock(stock);
                p.setCat(cat);

                dp.add(p);
                listarProductos(request, response);
            } else {
                response.sendRedirect("error.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }
    
    private void agregarCarrito(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		int pos=0;
    		cantidad=1;
    		int idp=Integer.parseInt(request.getParameter("id"));
    		Producto p = new Producto();
    		p=dp.getById(idp);
    		if(listaCarrito.size()>0) {
    			for(int i = 0; i<listaCarrito.size();i++) {
    				if(idp==listaCarrito.get(i).getIdProducto()) {
    					pos=i;    					
    				}
    			}
    			if(idp==listaCarrito.get(pos).getIdProducto()) {
    				cantidad=listaCarrito.get(pos).getCantidad()+cantidad;
    				double subtotal=listaCarrito.get(pos).getPrecioCompra()*cantidad;
    				listaCarrito.get(pos).setCantidad(cantidad);
    				listaCarrito.get(pos).setSubTotal(subtotal);
    			} else {
    				item=item+1;
    	    		Carrito car=new Carrito();
    	    		car.setItem(item);
    	    		car.setIdProducto(p.getId());
    	    		car.setNombres(p.getNombre());
    	    		car.setDescripcion(p.getDescripcion());
    	    		car.setPrecioCompra(p.getPrecio());
    	    		car.setCantidad(cantidad);
    	    		car.setSubTotal(cantidad*p.getPrecio());
    	    		listaCarrito.add(car);
    			}
    		}else {
	    		item=item+1;
	    		Carrito car=new Carrito();
	    		car.setItem(item);
	    		car.setIdProducto(p.getId());
	    		car.setNombres(p.getNombre());
	    		car.setDescripcion(p.getDescripcion());
	    		car.setPrecioCompra(p.getPrecio());
	    		car.setCantidad(cantidad);
	    		car.setSubTotal(cantidad*p.getPrecio());
	    		listaCarrito.add(car);}
    		
    		request.setAttribute("contador", listaCarrito.size());
    		request.getRequestDispatcher("Controller?accion=ListarCatalogo").forward(request, response);
    	}catch(Exception e){
    		e.printStackTrace();
            response.sendRedirect("error.jsp");
    	}
    }
    
    private void carritoDeCompras(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
    	try {
    		totalPagar=0.0;
    		request.setAttribute("carrito", listaCarrito);
    		for (int i =0; i<listaCarrito.size();i++) {
    			totalPagar=totalPagar+ listaCarrito.get(i).getSubTotal();
    		}
    		request.setAttribute("totalPagar", totalPagar);
    		request.getRequestDispatcher("carrito.jsp").forward(request, response);
    	}catch(Exception e) {
    		e.printStackTrace();
            response.sendRedirect("error.jsp");
    	}
    }
    
    private void comprarAhora(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		totalPagar=0.0;
    		int idp=Integer.parseInt(request.getParameter("id"));
    		Producto p = new Producto();
    		p=dp.getById(idp);
    		item=item+1;
    		Carrito car=new Carrito();
    		car.setItem(item);
    		car.setIdProducto(p.getId());
    		car.setNombres(p.getNombre());
    		car.setDescripcion(p.getDescripcion());
    		car.setPrecioCompra(p.getPrecio());
    		car.setCantidad(cantidad);
    		car.setSubTotal(cantidad*p.getPrecio());
    		listaCarrito.add(car);
    		for (int i =0; i<listaCarrito.size();i++) {
    			totalPagar=totalPagar+ listaCarrito.get(i).getSubTotal();
    		}
    		request.setAttribute("totalPagar", totalPagar);
    		request.setAttribute("carrito", listaCarrito);
    		request.setAttribute("contador", listaCarrito.size());
    		request.getRequestDispatcher("carrito.jsp").forward(request, response);
    	}catch(Exception e){
    		e.printStackTrace();
            response.sendRedirect("error.jsp");
    	}
    }
    
    private void actualizarCantidad(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	int idpro=Integer.parseInt(request.getParameter("idpro"));
    	int cant=Integer.parseInt(request.getParameter("Cantidad"));
    	for (int i =0; i<listaCarrito.size();i++) {
			if(listaCarrito.get(i).getIdProducto()==idpro) {
				listaCarrito.get(i).setCantidad(cant);
				double st=listaCarrito.get(i).getCantidad()*listaCarrito.get(i).getPrecioCompra();
				listaCarrito.get(i).setSubTotal(st);
			}
				
		}
    	
    	
    }
    
    private void eliminarProducto(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
    	int idDeleteP = Integer.parseInt(request.getParameter("id"));
        dp.delete(idDeleteP);
        
        listarProductos(request, response);
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }
}