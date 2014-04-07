class EmailAuth
  def self.generateTokens_from(user_token, bytes = 16)
    EmailAuth::Authenticator.generateTokens_from(user_token, bytes)
  end

  def self.generateTokens(bytes = 16)
    EmailAuth::Authenticator.generateTokens(bytes)
  end

  def self.authenticate(user_token, email_token, complete_token)
    EmailAuth::Authenticator.authenticate(user_token, email_token, complete_token)
  end

  def self.validateTOTP(auth_secret, code)
    EmailAuth::Authenticator.validateTOTP(auth_secret, code)
  end

  def self.generateTOTPSecret
    EmailAuth::Authenticator.generateTOTPSecret
  end
end

require 'emailauth/authenticator'
