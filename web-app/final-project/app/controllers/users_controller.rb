class UsersController < ApplicationController
  before_action :set_user, only: [:confirm, :twofactor, :show, :edit, :update, :destroy]

  # GET /users
  # GET /users.json
  def index
    @users = User.all
    @cur_user = current_user
  end

  def confirm
  end

  def create
    @user = User.new(user_params)
    if @user.save
      redirect_to :controller => "sessions", :action => "confirm", :handle => @user.handle
    else
      render 'new'
    end
  end

  def twofactor
    if(@user.auth_secret.nil?)
      @user.assign_auth_secret
    end
  end

  def new
    @user = User.new
  end

  # GET /users/1
  # GET /users/1.json
  def show
  end

  # GET /users/1/edit
  def edit
  end

  # PATCH/PUT /users/1
  # PATCH/PUT /users/1.json
  def update
    respond_to do |format|
      if @user.update(user_params)
        format.html { redirect_to @user, notice: 'User was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: 'edit' }
        format.json { render json: @user.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /users/1
  # DELETE /users/1.json
  def destroy
    @user.destroy
    respond_to do |format|
      format.html { redirect_to users_url }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_user
      @user = User.where(handle: params[:handle]).first
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def user_params
      params.require(:user).permit(:handle, :name, :pending_email, :user_token)
    end
end
