class Session < ActiveRecord::Base
  has_one :user, dependent: :destroy

  def authenticate(user_token, email_token)
    email_token_e = Base64.urlsafe_decode64(email_token)
    complete_token_e = Base64.urlsafe_decode64(auth_token)

    calculated_auth_token = Base64.urlsafe_encode64(complete_token_e.unpack('C*').zip(email_token_e.unpack('C*')).map{ |a,b| a ^ b }.pack('C*'))
    return user_token==calculated_auth_token
  end

  def Session.encrypt(token)
    Digest::SHA1.hexdigest(token.to_s)
  end
end
