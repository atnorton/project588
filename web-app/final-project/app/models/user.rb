class User < ActiveRecord::Base
  def assign_auth_secret
    self.auth_secret = ROTP::Base32.random_base32
    self.save!
  end
end
