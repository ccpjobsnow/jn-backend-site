package com.ccp.jn.web.spring.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ccp.decorators.CcpMapDecorator;
import com.ccp.jn.sync.service.JnSyncLoginService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping(value = "/login/{email}")
@Api(value = "Controles de login para cadastro de token, senha, senha fraca, pre registro, alem de controles de bloqueios diversos tais como: token, senha, senha de desbloqueio de token")
public class JnLoginController {
	
	private final JnSyncLoginService loginService = new JnSyncLoginService();
	
	@GetMapping
	public String teste(@PathVariable("email") String email) {
		return email;
	}
	
	@ApiOperation(value = "Nome deste passo: 'Executar Login'... Quando ocorre? Logo após o usuário digitar sua senha. Para que serve? Serve para o usuário executar login no sistema, gerando um token que será a prova "
			+ " (nas próximas requisições) que o requisitante (frontend), merece ter leitura ou escrita de certos recursos deste bando de dados. Passo anterior: 'Verificação de e-mail'."
			+ ", Exemplo  de JSON que este endpoint recebe: {'password': 'senhaqueousuariocadastrou', 'email': 'devs.jobsnow@gmail.com'} JSON que este endpoint retorna (em caso de status 200): "
			+ "{'sessionToken': 'tokengeradoaleatoriamente'}")
	 @ApiResponses(value = {
		        @ApiResponse(code = 200, message = "Status: 'Usuário logado com sucesso' <br/><br/> Quando ocorre? Quando o usuário digita senha e e-mail corretos e não está com senha ou token bloqueado ou pendência de desbloqueio de token<br/><br/>Qual comportamento esperado do front end? Que ele remova o modal de login e guarde o 'sessionToken' contido no json retornado por este endpoint"),
		        @ApiResponse(code = 201, message = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
		        @ApiResponse(code = 202, message = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
		        @ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
		        @ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
		        @ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
		        @ApiResponse(code = 409, message = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
		        @ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
		        @ApiResponse(code = 422, message = "Status: 'Senha digitada incorretamente' <br/><br/> Quando ocorre? Quando o usuário, digitou incorretamente a senha, mas ainda não excedeu o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Exibir mensagem de erro informando o número de tentativas incorretas de digitação de senha."),
		        @ApiResponse(code = 429, message = "Status: 'Senha recém bloqueada <br/><br/> Quando ocorre? No exato momento em que o usuário digitou incorretamente a senha, e acaba exceder o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário à tela de recadastro de senha."),
	 })	
	@PostMapping
	public Map<String, Object> executeLogin(HttpServletRequest request, @PathVariable("email") String email, @RequestBody Map<String, Object> body){
		String remoteAddr = request.getRemoteAddr();
		Map<String, Object> values = new CcpMapDecorator(body).put("ip", remoteAddr).put("email", email).content;
		CcpMapDecorator execute = this.loginService.executeLogin(values);
		return execute.content;
	}
	
	@ApiOperation(value = "Nome deste passo: 'Criar token para gerenciamento de senha'... Quando ocorre? Logo após ser constatado que é primeiro acesso deste usuário e ele confirmar o e-mail. Para que serve? Serve para o usuário requisitar envio de token para o seu e-mail e ele poder usar esse token para cadastrar senha. "
			+ " (nas próximas requisições) que o requisitante (frontend), merece ter leitura ou escrita de certos recursos deste bando de dados. Passo anterior: 'Verificação de e-mail'.")
	 @ApiResponses(value = {
			 	@ApiResponse(code = 200, message = "Status: 'Senha já cadastrada, usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
		        @ApiResponse(code = 201, message = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
		        @ApiResponse(code = 202, message = "Status: 'Token para cadastro de senha enviado ao e-mail do usuário' <br/><br/> Quando ocorre? Quando o usuário acaba de requisitar com sucesso, o cadastro de senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
		        @ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
		        @ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
		        @ApiResponse(code = 409, message = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
		        @ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	@PostMapping("/token/language/{language}")
	public Map<String, Object> createLoginToken(@PathVariable("email") String email, @PathVariable("language") String language) {
		CcpMapDecorator createLoginToken = this.loginService.createLoginToken(email, language);
		return createLoginToken.content;
	}
	
	@ApiOperation(value = "Nome deste passo: 'Verificação de existência deste usuário'... Quando ocorre? Logo após o usuário se interessar em ter acesso a informações deste sistema que ele só pode ter se estiver devidamente identificado (logado) nele. Para que serve? Serve para verificar se o usuário existe no sistema, caso ele existir, verificar se há pendências cadastrais (senha, pré registro) para ele resolver e se não existir, fazê-lo preencher todos os dados que o sistema precisa.")
	@ApiResponses(value = {
			 	@ApiResponse(code = 200, message = "Status: 'Senha já cadastrada, usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
		        @ApiResponse(code = 201, message = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
		        @ApiResponse(code = 202, message = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
		        @ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
		        @ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
		        @ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
		        @ApiResponse(code = 409, message = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
		        @ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
		        @ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	@RequestMapping(value = "/token", method = RequestMethod.HEAD)
	public void existsLoginToken(@PathVariable("email")String email) {
		this.loginService.existsLoginToken(email);
	}
	
	@ApiOperation(value = "Nome deste passo: 'Executar logout no sistema'... Quando ocorre? Quando por qualquer razão, o usuário quis não mais ter acesso a informações onde ele precisava estar devidamente identificado (logado) neste sistema. Para que serve? Serve para o usuário previamente se desassociar das próximas ações que serão feitas por este front end.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Usuário executou logout com sucesso' <br/><br/> Quando ocorre? Quando o usuário de fato estavacom sessão ativa (logado) neste sistema<br/><br/>Qual comportamento esperado do front end? Encerramento do modal de login."),
	        @ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
	        @ApiResponse(code = 404, message = "Status: 'Usuário não logado no sistema' <br/><br/> Quando ocorre? Quando o o usuário não está com sessão ativa neste sistema. <br/><br/>Qual comportamento esperado do front end? Encerramento do modal de login."),
	 })	
	@DeleteMapping
	public void executeLogout(@PathVariable("email") String email) {
		this.loginService.executeLogout(email);
	}


	@ApiOperation(value = "Nome deste passo: 'Ressolicitação de envio de token'... Quando ocorre? Quando o usuário, alegando não ter recebido o token de gerenciamento de senha, pede reenvio, por e-mail deste token. Para que serve? Serve para acionar o suporte do sistema para que manualmente entre em contato com o usuário, na intenção de lhe enviar corretamente o token.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Solicitação de reenvio de token executada com sucesso' <br/><br/> Quando ocorre? Quando o usuário consegue, com sucesso, solicitar o reenvio de token.<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que de cadastro de senha."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 409, message = "Status: 'Aguardando atendimento do suporte' <br/><br/> Quando ocorre? Quando o usuário acaba de solicitar com sucesso o seus desbloqueio de token, porém o suporte ainda não atendeu.<br/><br/>Qual comportamento esperado do front end? Apresentar mensagem de aviso ao usuário, sugerindo-o que aguarde resposta do suporte."),
			@ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	
	@PostMapping("/token/language/{language}/request/again")
	public Map<String, Object> requestTokenAgain(@PathVariable("email") String email, @PathVariable("language") String language) {
		CcpMapDecorator execute = this.loginService.requestTokenAgain(email, language);
		return execute.content;
	}

	@ApiOperation(value = "Nome deste passo: 'Solicitação para desbloqueio de token'... Quando ocorre? Logo após o usuário se bloquear seu token e então solicitar o desbloqueio. Para que serve? Serve para o sistema notificar o suporte para avaliar a solicitação de desbloqueio de token e, se for o caso, passar ao usuário uma senha temporária para que este possa fazer o desbloqueio de seu token.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Solicitação de desbloqueio de token executada com sucesso' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 409, message = "Status: 'Aguardando atendimento do suporte' <br/><br/> Quando ocorre? Quando o usuário acaba de solicitar com sucesso o seus desbloqueio de token, porém o suporte ainda não atendeu.<br/><br/>Qual comportamento esperado do front end? Apresentar mensagem de aviso ao usuário, sugerindo-o que aguarde resposta do suporte."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	
	@PostMapping("/token/language/{language}/unlocking")
	public Map<String, Object> requestUnlockToken(@PathVariable("email") String email, @PathVariable("language") String language) {
		CcpMapDecorator requestUnlockToken = this.loginService.requestUnlockToken(email, language);
		return requestUnlockToken.content;
	}
	
	@ApiOperation(value = "Nome deste passo: 'Salvar pré registro'... Quando ocorre? Logo após o usuário tentar executar login e o sistema constatar ausência de dados de pré registro. Para que serve? Serve para o usuário cadadtrar dados de pré registro.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(code = 202, message = "Status: 'O cadastro de  senha está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou cadastrar senha<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro da senha."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 409, message = "Status: 'Usuário já logado' <br/><br/> Quando ocorre? Quando já está registrada uma sessão corrente para o usuário que está tentando fazer login neste sistema. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	@PostMapping("/pre-registration")
	public void savePreRegistration(@PathVariable("email") String email,@RequestBody Map<String, Object> requestBody) {
		CcpMapDecorator cmd = new CcpMapDecorator(requestBody);
		CcpMapDecorator put = cmd.put("email", email);
		this.loginService.savePreRegistration(put);
	}
	
	
	@ApiOperation(value = "Nome deste passo: 'Confirmação de salvamento de senha fraca'... Quando ocorre? Logo após o usuário tentar cadastrar uma senha e ela ser constatada como fraca (de simples inferência). Para que serve? Serve para o usuário confirmar o cadastro e assumir o risco em cadastrar de uma senha fraca.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Que o modal de login desapareça, retornando o token da sessão."),
			@ApiResponse(code = 201, message = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
	 })	
	@PostMapping("/password/weak")
	public Map<String, Object> saveWeakPassword(@PathVariable("email") String email, @RequestBody Map<String, Object> requestBody) {
		CcpMapDecorator execute = this.loginService.saveWeakPassword(new CcpMapDecorator(requestBody).put("email", email));
		return execute.content;
	}
	
	@ApiOperation(value = "Nome deste passo: 'Desbloqueio de token'... Quando ocorre? Logo após o usuário se bloquear seu token, então solicitar o desbloqueio, receber do susporte do sistema a senha para desbloqueio de token e então, desbloquear o token. Para que serve? Desbloquear token de gerenciamento de senha.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Desbloqueio executado com sucesso' <br/><br/> Quando ocorre? Quando há o desbloqueio do token de gerenciamento de senhas<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro de senhas."),
			@ApiResponse(code = 202, message = "Status: 'Aguardando atendimento do suporte' <br/><br/> Quando ocorre? Quando o usuário acaba de solicitar com sucesso o seus desbloqueio de token, porém o suporte ainda não atendeu.<br/><br/>Qual comportamento esperado do front end? Apresentar mensagem de aviso ao usuário, sugerindo-o que aguarde resposta do suporte."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 401, message = "Status: 'Senha pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou logar várias vezes com a mesma senha incorreta.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de alteração de senha."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
			@ApiResponse(code = 422, message = "Status: 'Token não bloqueado' <br/><br/> Quando ocorre? Quando o usuário tenta o desbloqueio de um token que não está bloqueado. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário por meio de mensagem que ele está tentando desbloquear um token que não está bloqueado."),
	 })	
	@PatchMapping("/token/lock")
	public Map<String, Object> unlockToken(@PathVariable("email") String email, @RequestBody Map<String, Object> body) {
		CcpMapDecorator put = new CcpMapDecorator(body).put("email", email);
		CcpMapDecorator execute = this.loginService.unlockToken(put);
		return execute.content;
	}

	@ApiOperation(value = "Nome deste passo: 'Salvamento de senha'... Quando ocorre? Logo após o sistema constatar que o usuário está com senha bloqueada ou faltando, login já em uso ou se o usuário quer alterar senha. Para que serve? Serve para o usuário cadastrar senha de acesso no sistema.")
	@ApiResponses(value = {
		 	@ApiResponse(code = 200, message = "Status: 'Usuário sem pendências de cadastro' <br/><br/> Quando ocorre? Quando o usuário previamente cadastrou todos os dados de pre requisitos (token, senha e pré registro)<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela que pede senha para executar login."),
			@ApiResponse(code = 201, message = "Status: 'O cadastro de Pre registro está pendente' <br/><br/> Quando ocorre? Quando o usuário deixou de cadastrar dados do pré registro<br/><br/>Qual comportamento esperado do front end? Redirecionamento para a tela de cadastro do pré registro."),
			@ApiResponse(code = 400, message = "Status: 'Email inválido' <br/><br/> Quando ocorre? Quando a url path recebe um conjunto de caracteres que não representa um e-mail válido.<br/><br/>Qual comportamento esperado do front end? Apresentar erro genérico de sistema para o usuário."),
			@ApiResponse(code = 401, message = "Status: 'Token digitado incorretamente' <br/><br/> Quando ocorre? Quando o usuário, digitou incorretamente o token, mas ainda não excedeu o máximo de tentativas de senhas incorretas. <br/><br/>Qual comportamento esperado do front end? Exibir mensagem de erro informando o número de tentativas incorretas de digitação de token."),
			@ApiResponse(code = 403, message = "Status: 'Token bloqueado' <br/><br/> Quando ocorre? Quando o usuário, anteriormente tentou várias vezes alterar sua senha fazendo uso de token incorreto.<br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de requisição de desbloqueio de token."),
			@ApiResponse(code = 404, message = "Status: 'Usuário novo no sistema' <br/><br/> Quando ocorre? Quando o e-mail do usuário é desconhecido por este banco de dados. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para a tela de confirmação de e-mail."),
			@ApiResponse(code = 420, message = "Status: 'Token pendente de desbloqueio' <br/><br/> Quando ocorre? Quando o usuário bloqueou o token (digitando-o incorretamente por várias vezes na tela de alteração de senha) e então requisita desbloqueio de token, porém o suporte ainda não o atendeu. <br/><br/>Qual comportamento esperado do front end? Exibir uma mensagem de que em breve o suporte do JobsNow entrará em contato com ele por meio dos contatos informados e redirecioná-lo para a tela de desbloqueio de token."),
			@ApiResponse(code = 421, message = "Status: 'Senha de desbloqueio de token está bloqueada' <br/><br/> Quando ocorre? Quando o usuário, na tela de desbloqueio de token, por diversas vezes errou a digitação da senha de desbloqueio de token. <br/><br/>Qual comportamento esperado do front end? Informar ao usuário que ele está temporariamente bloqueado no acesso ao sistema e redirecioná-lo para a primeira tela do fluxo de login, para o caso de ele querer tentar com outro e-mail."),
			@ApiResponse(code = 422, message = "Status: 'A senha não cumpre requisitos para ser uma senha forte' <br/><br/> Quando ocorre? Quando a combinação de caracteres digitadas pelo usuário, não cumpre os requisitos para ser considerada uma senha forte. <br/><br/>Qual comportamento esperado do front end? Redirecionar o usuário para tela de confirmação de senha fraca."),
	 })	
	@PostMapping("/password")
	public Map<String, Object> updatePassword(@PathVariable("email") String email, @RequestBody Map<String, Object> requestBody) {
		CcpMapDecorator put = new CcpMapDecorator(requestBody).put("email", email);
		CcpMapDecorator execute = this.loginService.updatePassword(put);
		return execute.content;
	}


}
