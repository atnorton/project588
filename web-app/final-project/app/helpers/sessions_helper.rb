module SessionsHelper
  def qr_code(code)
    qr_url = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=#{code}"
    image_tag(qr_url, class: "center")
  end

  def log_out
    session_key = Session.encrypt(cookies.permanent[:session_id])
    session = Session.find_by(session_key: session_key)
    session.delete
    session.save
    cookies.delete(:session_id)
  end

  def log_in(user_token, email_token, validation_code = nil)
    session_key = Session.encrypt(user_token)
    session = Session.where(created_at: (Time.now - 5.minutes)..Time.now).find_by(session_key: session_key)
    if session==nil || session.logged_in || session.user.nil?
      return false
    end

    if(!session.user.auth_secret.nil? && !validate_twofactor(session.user, validation_code))
      return false
    end

    begin
      session.transaction do
        if !session.logged_in
          session.logged_in = true
          session.save!
          if session.authenticate(user_token, email_token)
            current_user = session.user
            return true
          end
        end
      end
    rescue
      return false
    end
  end

  def validate_twofactor(user, code)
    EmailAuth.validateTOTP(user.auth_secret, code)
  end

  def current_user=(user)
    @current_user = user
  end

  def get_user(user_token)
    session_key = Session.encrypt(cookies.permanent[:session_id])
    session = Session.find_by(session_key: session_key)
    if(!session.nil?)
      return session.user
    end
  end

  def current_user
    if cookies.permanent[:session_id]==nil
      return nil
    end
    session_key = Session.encrypt(cookies.permanent[:session_id])
    session = Session.find_by(session_key: session_key)
    if session!=nil && session.logged_in && session.user!=nil
      @current_user = session.user
    else
      @current_user = nil
    end
  end

  def signed_in?
    !current_user.nil?
  end

  def logged_in?(user)
    current_user==user
  end
end
