class Session < ActiveRecord::Base
  belongs_to :user

  def authenticate(user_token, email_token)
    EmailAuth.authenticate(user_token, email_token, auth_token)
  end

  def Session.encrypt(token)
    Digest::SHA1.hexdigest(token.to_s)
  end

  def Session.generateSessionId
    SecureRandom.urlsafe_base64(32)
  end
end
