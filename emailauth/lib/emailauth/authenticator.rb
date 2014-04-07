require 'securerandom'
require 'base64'
require 'rotp'

class EmailAuth::Authenticator
  ##
  # Generates SAW tokens in base64 given a user_token
  ##
  def self.generateTokens_from(user_token_s, bytes = 16)
    user_token = Base64.urlsafe_decode64(user_token_s)

    email_token = SecureRandom.random_bytes(bytes)
    email_token_s = Base64.urlsafe_encode64(email_token)

    complete_token_s = Base64.urlsafe_encode64(user_token.unpack('C*').zip(email_token.unpack('C*')).map{ |a,b| a ^ b }.pack('C*'))

    return [email_token_s,complete_token_s]
  end

  ##
  # Generates SAW tokens in base64 that are the provided number of bytes
  ##
  def self.generateTokens(bytes = 16)
    complete_token = SecureRandom.random_bytes(bytes)
    complete_token_s = Base64.urlsafe_encode64(complete_token)
    email_token = SecureRandom.random_bytes(bytes)
    email_token_s = Base64.urlsafe_encode64(email_token)

    user_token_s = Base64.urlsafe_encode64(complete_token.unpack('C*').zip(email_token.unpack('C*')).map{ |a,b| a ^ b }.pack('C*'))

    return [user_token_s,email_token_s,complete_token_s]
  end

  ##
  # Validates whether the provided tokens are correct using SAW protocol
  ##
  def self.authenticate(user_token, email_token, complete_token)
    email_token_e = Base64.urlsafe_decode64(email_token)
    complete_token_e = Base64.urlsafe_decode64(complete_token)

    calculated_user_token = Base64.urlsafe_encode64(complete_token_e.unpack('C*').zip(email_token_e.unpack('C*')).map{ |a,b| a ^ b }.pack('C*'))
    return user_token==calculated_user_token
  end

  ##
  # Validates whether a TOTP code is valid for a particular secret.
  # Allows for a 30 second max transmission delay
  ##
  def self.validateTOTP(auth_secret, code)
    return ROTP::TOTP.new(auth_secret).verify_with_drift(code, 30)
  end

  ##
  # Generates a TOTP secret
  ##
  def self.generateTOTPSecret
    ROTP::Base32.random_base32
  end
end
