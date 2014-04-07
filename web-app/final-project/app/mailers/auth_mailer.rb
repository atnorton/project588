class AuthMailer < ActionMailer::Base
  default from: "send.auth@outlook.com"

  def auth_email(email, token, remote_ip)
    @ip = remote_ip
    @email_token = token
    mail(to: email, subject: "Log in request")
  end
end
