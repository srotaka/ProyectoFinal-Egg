package grupo7.egg.nutrividas.controlador;

import grupo7.egg.nutrividas.entidades.Credencial;
import grupo7.egg.nutrividas.entidades.Usuario;
import grupo7.egg.nutrividas.exeptions.BadCredentialsException;
import grupo7.egg.nutrividas.mail.MailService;
import grupo7.egg.nutrividas.servicios.CredencialServicio;
import grupo7.egg.nutrividas.servicios.FotoServicio;
import grupo7.egg.nutrividas.servicios.UsuarioServicio;
import grupo7.egg.nutrividas.entidades.Comedor;
import grupo7.egg.nutrividas.enums.Provincia;
import grupo7.egg.nutrividas.servicios.ComedorServicio;
import grupo7.egg.nutrividas.entidades.Nutricionista;
import grupo7.egg.nutrividas.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@Controller
public class PrincipalControlador {

    @Autowired
    private ProvinciaServicio provinciaServicio;

    @Autowired
    private FotoServicio fotoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private NutricionistaServicio nutricionistaServicio;

    @Autowired
    private ComedorServicio comedorServicio;

    @Autowired
    private MailService mailService;

    @Autowired
    private CredencialServicio credencialServicio;



    @GetMapping("/")
    public ModelAndView inicio(){
        return new ModelAndView("index");
    }
    
    @GetMapping("/politica")
    public ModelAndView politica(){
        return new ModelAndView("politica-privacidad");
    }

    @GetMapping("/condiciones")
    public ModelAndView condiciones(){
        return new ModelAndView("terminos-condiciones");
    }

    @GetMapping("/contacto")
    public ModelAndView contacto(){
        return new ModelAndView("contacto");
    }
    
    @PostMapping("/confirmar")
    public RedirectView confirmacion(@RequestParam("tokenMail")Long tokenMail){
        credencialServicio.habilitarCuenta(tokenMail);
        return new RedirectView("/confirmado");
    }

    @GetMapping("/confirmado")
    public ModelAndView confirmado(){
        return new ModelAndView("confirmacion-mail");
    }
    
    @GetMapping("/pago")
    public ModelAndView pago(){
        return new ModelAndView("pago");
    }

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(required = false) String error, @RequestParam(required = false)String logout, Principal principal, HttpServletRequest request){

        ModelAndView modelAndView = new ModelAndView("login");

        Map<String,?> flashMap = RequestContextUtils.getInputFlashMap(request);
        if (error != null) {
            modelAndView.addObject("error", "Usuario o contraseña incorrectos");
        }

        if (logout != null) {
            modelAndView.addObject("logout", "Ha salido correctamente de la plataforma");
             modelAndView.setViewName("redirect:/");
        }
        if(flashMap != null){
            modelAndView.addObject("success", flashMap.get("success"));
        }

        if (principal != null) {
            modelAndView.setViewName("redirect:/ ");
        }


        if(principal!= null && credencialServicio.findByMail(principal.getName()).getHabilitado() == false ){
            System.out.println("Mail: "+principal.getName());
            throw new BadCredentialsException("La cuenta se encuentra inhabilitada");
        }

        return modelAndView;
    }

    @GetMapping(value= "/seleccion")
    public ModelAndView seleccion(@RequestParam(required = false) String error, HttpServletRequest request, Principal principal){
        ModelAndView modelAndView = new ModelAndView("seleccionUsuario");

        Map<String,?> flashMap = RequestContextUtils.getInputFlashMap(request);
        if (error != null) {
            modelAndView.addObject("error", "Usuario o contraseña incorrectos");
        }

        if(flashMap != null){
            modelAndView.addObject("success", flashMap.get("success"));
        }

        if (principal != null) {
            modelAndView.setViewName("redirect:/ ");
        }

        return modelAndView;
    }

    @GetMapping(value = "/signup/usuario")
    public ModelAndView signup(HttpServletRequest request,Principal principal){
        ModelAndView mav = new ModelAndView("signup");
        Map<String,?> flashMap = RequestContextUtils.getInputFlashMap(request);

        if (principal != null) {
            mav.setViewName("redirect:/ ");
        }

        if (flashMap != null) {
            mav.addObject("error", flashMap.get("error"));
            mav.addObject("usuario", flashMap.get("usuario"));
        } else {
            mav.addObject("usuario", new Usuario());
        }

        return mav;
    }

    @Value("${picture.users.location}")
    public String USUARIOS_UPLOADED_FOLDER;

    @PostMapping(value = "/registro/usuario")
    public ModelAndView saveCustomer(@Valid @ModelAttribute Usuario usuario, BindingResult result, HttpServletRequest request, RedirectAttributes attributes){

        ModelAndView mav = new ModelAndView();

        if (result.hasErrors()) {
            mav.addObject("usuario", usuario);
            mav.setViewName("signup");
            return mav;
        }

        try {

            Usuario usuarioCreado =usuarioServicio.crearUsuario(usuario.getDni(), usuario.getNombre(), usuario.getApellido(), usuario.getCredencial().getMail(), usuario.getTelefono(),usuario.getCredencial().getUsername(),usuario.getCredencial().getPassword());
            mailService.sendWelcomeMail("Bienvenida",usuario.getCredencial().getMail(),usuario.getCredencial().getUsername(),usuarioCreado.getCredencial().getId(),"USUARIO");
            //request.login(usuario.getCredencial().getMail(), usuario.getCredencial().getPassword());
            mav.setViewName("redirect:/");
        } catch (Exception e) {
            attributes.addFlashAttribute("usuario", usuario);
            attributes.addFlashAttribute("error", e.getMessage());
            mav.setViewName("redirect:/signup/usuario");
        }


        return mav;
    }

    @GetMapping(value = "/signup/nutricionista")
    public ModelAndView signupNutricionista(HttpServletRequest request,Principal principal){
        ModelAndView mav = new ModelAndView("signupNutricionista");
        Map<String,?> flashMap = RequestContextUtils.getInputFlashMap(request);

        if (principal != null) {
            mav.setViewName("redirect:/ ");
        }

        if (flashMap != null) {
            mav.addObject("error", flashMap.get("error"));
            mav.addObject("nutricionista", flashMap.get("nutricionista"));
        } else {
            mav.addObject("nutricionista", new Nutricionista());
        }

        return mav;
    }

    @PostMapping(value = "/registro/nutricionista")
    public ModelAndView saveNutricionista(@Valid @ModelAttribute Nutricionista nutricionista, BindingResult result, HttpServletRequest request, RedirectAttributes attributes){

        ModelAndView mav = new ModelAndView();


        if (result.hasErrors()) {
            mav.addObject("nutricionista", nutricionista);
            mav.setViewName("signupNutricionista");
            return mav;
        }

        try {

            Nutricionista nutricionistaCreado =nutricionistaServicio.crearNutricionista(nutricionista.getDocumento(), nutricionista.getNombre(),
                    nutricionista.getApellido(), nutricionista.getMatricula(), nutricionista.getFechaNacimiento(),
                    nutricionista.getTelefono(), nutricionista.getCredencial().getMail(),
                    nutricionista.getCredencial().getUsername(),nutricionista.getCredencial().getPassword());

            /*Foto foto;
            if(usuario.getFoto() == null){
                foto = fotoServicio.crearFoto(USUARIOS_UPLOADED_FOLDER,String.valueOf(usuarioCreado.getId()),usuario.getNombre()+"-"+usuario.getApellido(),usuario.getFoto());
            }else{
                foto = fotoServicio.actualizarFoto(usuarioCreado.getFoto(),USUARIOS_UPLOADED_FOLDER,String.valueOf(usuarioCreado.getId()),usuario.getNombre()+"-"+usuario.getApellido(),multipartFile);
            }
            usuarioServicio.crearFoto(foto,usuario.getId());*/
            mailService.sendWelcomeMail("Bienvenida",nutricionistaCreado.getCredencial().getMail(),nutricionista.getCredencial().getUsername(),nutricionista.getCredencial().getId(),"NUTRICIONISTA");
            //request.login(nutricionista.getCredencial().getMail(), nutricionista.getCredencial().getPassword());
            mav.setViewName("redirect:/");

        } catch (Exception e) {
            attributes.addFlashAttribute("nutricionista", nutricionista);
            attributes.addFlashAttribute("error", e.getMessage());
            mav.setViewName("redirect:/signup/nutricionista");
        }


        return mav;
    }

    @GetMapping(value = "/signup/comedor")
    public ModelAndView signupComedor(HttpServletRequest request,Principal principal){
        ModelAndView mav = new ModelAndView("signupComedor");
        Map<String,?> flashMap = RequestContextUtils.getInputFlashMap(request);
        mav.addObject("provincias", provinciaServicio.obtenerProvincias());

        if (principal != null) {
            mav.setViewName("redirect:/ ");
        }

        if (flashMap != null) {
            mav.addObject("error", flashMap.get("error"));
            mav.addObject("comedor", flashMap.get("comedor"));
        } else {

            mav.addObject("comedor", new Comedor());
        }

        return mav;
    }

    @PostMapping(value = "/registro/comedor")
    public ModelAndView saveComedor(@Valid @ModelAttribute Comedor comedor, BindingResult result, HttpServletRequest request, RedirectAttributes attributes){

        ModelAndView mav = new ModelAndView("signupComedor");
        if (result.hasErrors()) {
            mav.addObject("comedor", comedor);
            mav.setViewName("signupComedor");
            return mav;
        }

        try {
            Comedor comedorCreado = comedorServicio.crearComedor(comedor.getNombre(), comedor.getDireccion().getCalle(), comedor.getDireccion().getNumero(), comedor.getDireccion().getCodigoPostal(), comedor.getDireccion().getLocalidad(), comedor.getDireccion().getProvincia(), comedor.getCantidadDePersonas(), comedor.getTelefono(), comedor.getCredencial().getUsername(), comedor.getCredencial().getMail(), comedor.getCredencial().getPassword());

            request.login(comedor.getCredencial().getMail(), comedor.getCredencial().getPassword());
            mav.setViewName("redirect:/");
        } catch (ServletException e) {
            attributes.addFlashAttribute("error", "Error al realizar auto-login");
        } catch (Exception e) {
            attributes.addFlashAttribute("comedor", comedor);
            attributes.addFlashAttribute("error", e.getMessage());
            mav.setViewName("redirect:/signup/comedor");
        }


        return mav;
    }

    @GetMapping(value = "/modificar/{username}")
    public ModelAndView editarPerfil(@PathVariable String username, HttpServletRequest request, RedirectAttributes attributes){

        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);
        ModelAndView mav;
        Credencial credencial = credencialServicio.buscarCredencialPorUsername(username);
        Long id = credencial.getId();

        if(usuarioServicio.buscarUsuarioPorCredencial(id) != null){
            mav = new ModelAndView("editarUsuario");
            return modificarUsuario(id, mav, flashMap, attributes);

            }else if(comedorServicio.buscarComedorPorCredencial(id) != null){
                Comedor comedor = comedorServicio.buscarComedorPorCredencial(id);
                mav = new ModelAndView("editarComedores");
                mav.addObject("comedor", comedorServicio.buscarComedorPorCredencial(id));
                return mav;
                }else{
                    Nutricionista nutricionista = nutricionistaServicio.buscarNutricionistaPorCredencial(id);
                    mav = new ModelAndView("editarNutricionista");

                }
        return mav;
    }

    public ModelAndView modificarUsuario(Long id, ModelAndView mav, Map<String, ?> flashMap, RedirectAttributes attributes){
        try {
            if(flashMap != null){
                mav.addObject("error", flashMap.get("error"));
            }else {
                mav.addObject("usuario", usuarioServicio.buscarUsuarioPorCredencial(id));
            }
            mav.addObject("title", "Editar Usuario");
        }catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            mav.setViewName("redirect:/");
        }
        return mav;
    }

    @PostMapping("/modificar/usuario")
    public RedirectView modificar(@RequestParam Long id, @RequestParam String nombre, @RequestParam String apellido, @RequestParam Long dni, @RequestParam Long telefono, RedirectAttributes redirect, RedirectAttributes attributes){
        RedirectView redirectView = new RedirectView("/");
        Usuario usuario = usuarioServicio.buscarPorId(id);

        try{
           usuarioServicio.modificarUsuario(id, dni, nombre, apellido, telefono);

        }catch(Exception e){
            redirect.addFlashAttribute("error", e.getMessage());
            redirectView.setUrl("/modificar/{" + usuario.getCredencial().getUsername()+"}");
            return redirectView;
        }
        return redirectView;
    }

    public ModelAndView modificarComedor(Long id, ModelAndView mav, Map<String, ?> flashMap, RedirectAttributes attributes){
        try {
            if(flashMap != null){
                mav.addObject("error", flashMap.get("error"));
            }else {
                mav.addObject("comedor", comedorServicio.buscarComedorPorCredencial(id));
            }
            mav.addObject("title", "Editar Comedor");
        }catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            mav.setViewName("redirect:/");
        }
        return mav;
    }

    /*@PostMapping("/modificar/comedor")
    public RedirectView modificar(@RequestParam Long id, @RequestParam String nombre, @RequestParam String apellido, @RequestParam Long dni, @RequestParam Long telefono, RedirectAttributes redirect, RedirectAttributes attributes){
        RedirectView redirectView = new RedirectView("/");
        Comedor comedor = comedorServicio.buscarPorId(id);

        try{
            comedorServicio.modificarComedor(id, nombre, calle, numero, codigoPostal, apellido, telefono);

        }catch(Exception e){
            redirect.addFlashAttribute("error", e.getMessage());
            redirectView.setUrl("/modificar/{" + comedor.getCredencial().getUsername()+"}");
            return redirectView;
        }
        return redirectView;
    }*/


}


