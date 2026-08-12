# 使用 GitHub 登录
本节介绍如何使用 GitHub 作为身份验证提供程序来配置示例应用程序，并涵盖以下主题：
<ul>
<li>[注册 OAuth 应用程序](https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/java/oauth2/login/README.adoc#github-register-application)</li>
<li>[配置application.yml](https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/java/oauth2/login/README.adoc#github-application-config)</li>
<li>[启动应用程序](https://github.com/spring-projects/spring-security-samples/blob/main/servlet/spring-boot/java/oauth2/login/README.adoc#github-boot-application)</li>
</ul>

## 注册 OAuth 应用程序
<p>要使用 GitHub 的 OAuth 2.0 身份验证系统进行登录，您必须[注册一个新的 OAuth 应用程序](https://github.com/settings/applications/new)。</p>
<p>注册 OAuth 应用程序时，确保授权回调 URL设置为http://127.0.0.1:8080/login/oauth2/code/github。</p>
<p>授权回调 URL（重定向 URI）是最终用户的用户代理在通过 GitHub 进行身份验证并在授权应用程序页面上授予对 OAuth 应用程序的访问权限后重定向回的应用程序中的路径。</p>
<table style="width:100%; border-collapse:collapse;">
<tr>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">提示</td>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">默认重定向 URI 模板是{baseUrl}/login/oauth2/code/{registrationId}。 RegistrationId是的唯一标识符ClientRegistration。</td>
</tr>
<tr>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">重要的</td>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">如果应用程序在代理服务器后面运行，建议检查[代理服务器配置](https://docs.spring.io/spring-security/reference/)以确保应用程序配置正确。另请参阅支持的模板[URI变量](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html#oauth2Client-auth-code-redirect-uri)redirect-uri。</td>
</tr>
</table>

## 配置application.yml
<p>现在您已经有了 GitHub 的新 OAuth 应用程序，您需要配置该应用程序以使用 OAuth 应用程序进行身份验证流程。为此：</p>
<ul>
<li>转到application.yml并设置以下配置：</li>

```
spring:
  security:
    oauth2:
      client:
        registration:	(1)
          github:		(2)
            client-id: github-client-id
            client-secret: github-client-secret
```
<li>1. 示例 3.OAuth 客户端属性</li>
<ol>1.1 spring.security.oauth2.client.registration是 OAuth 客户端属性的基本属性前缀。</ol>
<ol>1.2 基本属性前缀后面是 的 ID ClientRegistration，例如 github。</ol>
<li>2. client-id将和属性中的值替换client-secret为您之前创建的 OAuth 2.0 凭据。</li>
</ul>


## 启动应用程序
<p>启动 Spring Boot 2.0 示例并转到http://127.0.0.1:8080.然后，您将被重定向到默认的自动生成的登录页面，其中显示 GitHub 的链接。</p>
<p>单击 GitHub 链接，然后您将被重定向到 GitHub 进行身份验证。</p>
<p>使用 GitHub 凭据进行身份验证后，出现的下一页是“授权应用程序”。此页面将要求您授权您在上一步中创建的应用程序。单击授权应用程序以允许 OAuth 应用程序访问您的个人用户数据信息。</p>
<p>此时，OAuth 客户端从 UserInfo 端点检索您的个人用户信息并建立经过身份验证的会话。</p>
<table>
<tr>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">提示</td>
<td style="border:2px solid #dddddd; padding:8px; text-align:left; vertical-align:middle;">有关 UserInfo Endpoint 返回的详细信息，请参阅 API 文档中的“[获取经过身份验证的用户](https://docs.github.com/en/rest/users?apiVersion=2022-11-28#get-the-authenticated-user)”。</td>
</tr>
</table>