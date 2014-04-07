class User < ActiveRecord::Base
  has_many :sessions, dependent: :destroy

  VALID_EMAIL_REGEX = /\A[\w+\-.]+@[a-z\d\-.]+\.[a-z]+\z/i
  validates :email, presence: true, format: { with: VALID_EMAIL_REGEX }

  def assign_auth_secret
    self.auth_secret = EmailAuth.generateTOTPSecret
    self.save!
    return self.auth_secret
  end
end
