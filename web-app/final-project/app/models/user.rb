class User < ActiveRecord::Base
  def assign_auth_secret
    self.auth_secret = EmailAuth.generateTOTPSecret
    self.save!
    return self.auth_secret
  end
end
